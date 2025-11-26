package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.interfaces.LeaderStatusQueryService;
import com.avatar.avatar_online.raft.model.LeaderInfo;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;

import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ClusterLeadershipService implements LeaderStatusQueryService {

    private final HazelcastInstance hazelcast;
    private final LeaderRegistryService leaderRegistryService;
    private final LogConsensusService  logConsensusService;
    private final LogStore logStore;
    private final DatabaseSyncService syncService; // Injetado no construtor

    private ScheduledExecutorService heartbeatScheduler;
    private ScheduledExecutorService electionScheduler;
    private ScheduledExecutorService cleanupScheduler;
    private AtomicBoolean isLeader = new AtomicBoolean(false);
    private AtomicBoolean electionActive = new AtomicBoolean(false);
    private String currentNodeId;

    private static final String LEADER_ELECTION_MAP = "leader-election";
    private static final String LEADER_KEY = "current-leader-node";
    private static final long HEARTBEAT_INTERVAL_MS = 100;


    public ClusterLeadershipService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                                    LeaderRegistryService leaderRegistryService,
                                    LogConsensusService logConsensusService, LogStore logStore,
                                    DatabaseSyncService syncService) {
        this.hazelcast = hazelcast;
        this.leaderRegistryService = leaderRegistryService;
        this.logConsensusService = logConsensusService;
        this.logStore = logStore;
        this.syncService = syncService;
    }

    public void init() {
        this.currentNodeId = hazelcast.getCluster().getLocalMember().getUuid().toString();
        this.electionScheduler = Executors.newSingleThreadScheduledExecutor();
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

        startLeaderElection();
        startCleanupTask();
        setupClusterListeners();

        System.out.println("🚀 Serviço de liderança inicializado - Nó: " + currentNodeId);
    }

    /**
     * Algoritmo de eleição de líder baseado em IMap
     */
    private void startLeaderElection() {
        electionScheduler.scheduleAtFixedRate(() -> {
            if (electionActive.get()) return;
            
            electionActive.set(true);
            try {
                performLeaderElection();
            } finally {
                electionActive.set(false);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void performLeaderElection() {
        IMap<String, String> electionMap = hazelcast.getMap(LEADER_ELECTION_MAP);

        String previousLeaderId = electionMap.putIfAbsent(LEADER_KEY, currentNodeId, 10, TimeUnit.SECONDS);

        if ((previousLeaderId == null || previousLeaderId.equals(currentNodeId)) && getClusterSize() > 1) {

            if (!isLeader.get() || previousLeaderId == null) {

                if (isLogUpToDate()) {

                    long newTerm = leaderRegistryService.incrementTerm();

                    onBecomeLeader(newTerm);
                } else {
                    System.out.println("❌ Eleição falhou: Nó " + currentNodeId +
                            " possui Termo/Log desatualizado. Removendo lock.");
                    electionMap.remove(LEADER_KEY, currentNodeId); // Remove o próprio lock
                    return;
                }
            }

            if (isLeader.get()) {
                electionMap.put(LEADER_KEY, currentNodeId, 10, TimeUnit.SECONDS);
            }

        } else {
            LeaderInfo currentLeader = leaderRegistryService.getCurrentLeader();

            if (isLeader.get() && (currentLeader == null || !currentLeader.getNodeId().equals(currentNodeId))) {
                onLostLeadership();
            }
        }
    }

    private boolean isLogUpToDate() {
        long myIndex = logStore.getLastIndex();

        Map<String, Long> allLogs = logConsensusService.getAllNodeLogIndices();

        long maxIndex = allLogs.values().stream()
                .max(Long::compare)
                .orElse(0L);
        boolean upToDate = (myIndex >= maxIndex);

        if (!upToDate) {
            System.out.println("⚠️ Validação de Log: Meu índice (" + myIndex +
                    ") é menor que o índice máximo (" + maxIndex + ").");
        }

        return upToDate;
    }

    private void onBecomeLeader(long term) {
        isLeader.set(true);
        String nodeInfo = hazelcast.getCluster().getLocalMember().getAddress().toString();
        System.out.println("🎯 EU SOU O LÍDER AGORA! Nó: " + nodeInfo + ", Termo: " + term);

        // Registra como líder no cluster
        leaderRegistryService.registerAsLeader(term);

        logConsensusService.updateLastCommittedIndex(logStore.getLastCommitIndex());

        long lastLogIndex = logStore.getLastIndex();
        logConsensusService.initializeLeaderState(lastLogIndex);

        startHeartbeatScheduler();
    }

    private void onLostLeadership() {
        isLeader.set(false);
        System.out.println("👥 Perdi a liderança. Agora sou seguidor.");

        // Remove registro de líder
        leaderRegistryService.unregisterAsLeader();

        stopHeartbeatScheduler();
    }

    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                leaderRegistryService.cleanupExpiredLeaders();

                // Limpa líderes expirados no mapa de eleição
                IMap<String, String> electionMap = hazelcast.getMap(LEADER_ELECTION_MAP);
                String currentLeader = electionMap.get(LEADER_KEY);

                if (currentLeader != null) {
                    // Verifica se o líder ainda está no cluster
                    boolean leaderStillInCluster = hazelcast.getCluster().getMembers().stream()
                            .anyMatch(member -> member.getUuid().toString().equals(currentLeader));

                    if (!leaderStillInCluster) {
                        electionMap.remove(LEADER_KEY);
                        System.out.println("🧹 Líder expirado removido da eleição: " + currentLeader);
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Erro na limpeza: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void setupClusterListeners() {
        hazelcast.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent event) {
                String newMemberId = event.getMember().getUuid().toString();
                System.out.println("🟢 Novo nó entrou: " + event.getMember() + " [ID: " + newMemberId + "]");

                    // Se este nó é o novo nó que entrou, sincroniza com líder
                    if (newMemberId.equals(currentNodeId)) {
                        System.out.println("🆕 Este é o novo nó - sincronizando com líder...");
                    }
            }

            @Override
            public void memberRemoved(MembershipEvent event) {
                String removedMemberId = event.getMember().getUuid().toString();
                System.out.println("🔴 Nó saiu: " + event.getMember() + " [ID: " + removedMemberId + "]");

                // Se o líder saiu, força nova eleição
                IMap<String, String> electionMap = hazelcast.getMap(LEADER_ELECTION_MAP);
                String currentLeader = electionMap.get(LEADER_KEY);

                if (removedMemberId.equals(currentLeader)) {
                    System.out.println("⚡ Líder saiu - forçando nova eleição");
                    electionMap.remove(LEADER_KEY);
                }
            }
        });
    }

    public boolean isLeader() {
        return isLeader.get();
    }

    public String getLeaderInfo() {
        LeaderInfo leader = leaderRegistryService.getCurrentLeader();
        if (leader != null) {
            return isLeader.get() ?
                    "Este nó é o LÍDER (" + leader.getHttpAddress() + ")" :
                    "Este nó é SEGUIDOR. Líder: " + leader.getHttpAddress();
        } else {
            // Fallback para mapa de eleição
            IMap<String, String> electionMap = hazelcast.getMap(LEADER_ELECTION_MAP);
            String electionLeader = electionMap.get(LEADER_KEY);

            if (electionLeader != null && electionLeader.equals(currentNodeId)) {
                return "Este nó é o LÍDER (eleição)";
            } else if (electionLeader != null) {
                return "Este nó é SEGUIDOR. Líder da eleição: " + electionLeader;
            } else {
                return "Líder não definido - Em processo de eleição";
            }
        }
    }

    public int getClusterSize() {
        return hazelcast.getCluster().getMembers().size();
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public boolean hasActiveLeader() {
        IMap<String, String> electionMap = hazelcast.getMap(LEADER_ELECTION_MAP);
        String currentLeader = electionMap.get(LEADER_KEY);

        if (currentLeader == null) {
            return false;
        }

        // Verifica se o líder ainda está no cluster
        return hazelcast.getCluster().getMembers().stream()
                .anyMatch(member -> member.getUuid().toString().equals(currentLeader));
    }

    private void startHeartbeatScheduler() {
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "Raft-Heartbeat-Leader")
        );
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                LeaderInfo currentLeader = leaderRegistryService.getCurrentLeader();
                currentLeader.updateHeartbeat();
                leaderRegistryService.putInLeaderMap(currentLeader);
                syncService.sendHeartbeat();
            } catch (Exception e) {
                System.err.println("❌ Erro no Heartbeat do líder: " + e.getMessage());
            }
        }, 0, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void stopHeartbeatScheduler() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdownNow();
            this.heartbeatScheduler = null;
        }
    }
}