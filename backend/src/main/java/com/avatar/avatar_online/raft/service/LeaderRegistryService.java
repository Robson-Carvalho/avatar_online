package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.config.NodeIDConfig;
import com.avatar.avatar_online.raft.model.LeaderInfo;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LeaderRegistryService {
    private final HazelcastInstance hazelcast;
    private final String nodeId;
    private ScheduledExecutorService heartbeatScheduler;
    private static final String LEADER_REGISTRY_MAP = "leader-registry";
    private static final String LEADER_KEY = "current-leader";
    private static final String CURRENT_TERM_MAP = "current-term-map";
    private static final String TERM_KEY = "last-known-term";
    private static final String TERM_LOCK_KEY = "term-lock";
    private static final String LOG_INDEX_MAP = "node-log-index";
    private static final String LOG_INDEX_KEY_PREFIX = "log-index-node-";

    @Value("${app.server.port:8080}")
    private int serverPort;

    @Autowired
    public LeaderRegistryService(HazelcastInstance hazelcast, NodeIDConfig nodeIDConfig) {
        this.hazelcast = hazelcast;
        this.nodeId = nodeIDConfig.getNodeId(); // ← Agora funciona!
    }

    public LeaderRegistryService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast, String nodeId) {
        this.hazelcast = hazelcast;
        this.nodeId = nodeId;
    }

    @PostConstruct
    public void init() {
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Registra este nó como líder no cluster
     */
    public void registerAsLeader(long term) {
        String currentHost = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        LeaderInfo leaderInfo = new LeaderInfo(nodeId, currentHost, serverPort, term);

        IMap<String, LeaderInfo> leaderMap = hazelcast.getMap(LEADER_REGISTRY_MAP);
        leaderMap.put(LEADER_KEY, leaderInfo);

        System.out.println("✅ Registrado como líder: " + leaderInfo);

        // Inicia heartbeat para manter o registro
        startHeartbeat();
    }

    /**
     * Remove registro de líder (quando perde liderança)
     */
    public void unregisterAsLeader() {
        IMap<String, LeaderInfo> leaderMap = hazelcast.getMap(LEADER_REGISTRY_MAP);
        LeaderInfo currentLeader = leaderMap.get(LEADER_KEY);

        if (currentLeader != null && currentLeader.getNodeId().equals(nodeId)) {
            leaderMap.remove(LEADER_KEY);
            System.out.println("🗑️  Registro de líder removido");
        }

        stopHeartbeat();
    }

    /**
     * Obtém o líder atual registrado
     */
    public LeaderInfo getCurrentLeader() {
        IMap<String, LeaderInfo> leaderMap = hazelcast.getMap(LEADER_REGISTRY_MAP);
        LeaderInfo leader = leaderMap.get(LEADER_KEY);

        // Verifica se o líder não está expirado
        if (leader != null && leader.isExpired(45000)) { // 45 segundos
            System.out.println("⚠️  Líder expirado: " + leader);
            leaderMap.remove(LEADER_KEY);
            return null;
        }

        return leader;
    }

    /**
     * Verifica se este nó é o líder registrado
     */
    public boolean isRegisteredLeader() {
        LeaderInfo leader = getCurrentLeader();
        return leader != null && leader.getNodeId().equals(nodeId);
    }

    /**
     * Atualiza heartbeat do líder
     */
    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                LeaderInfo currentLeader = getCurrentLeader();
                if (currentLeader != null && currentLeader.getNodeId().equals(nodeId)) {
                    // Atualiza heartbeat
                    currentLeader.updateHeartbeat();
                    IMap<String, LeaderInfo> leaderMap = hazelcast.getMap(LEADER_REGISTRY_MAP);
                    leaderMap.put(LEADER_KEY, currentLeader);
                    System.out.println("💓 Heartbeat do líder atualizado");
                }
            } catch (Exception e) {
                System.err.println("❌ Erro no heartbeat: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS); // Heartbeat a cada 10 segundos
    }

    private void stopHeartbeat() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
            try {
                if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Limpa registros expirados
     */
    public void cleanupExpiredLeaders() {
        IMap<String, LeaderInfo> leaderMap = hazelcast.getMap(LEADER_REGISTRY_MAP);
        LeaderInfo leader = leaderMap.get(LEADER_KEY);

        if (leader != null && leader.isExpired(45000)) {
            leaderMap.remove(LEADER_KEY);
            System.out.println("🧹 Líder expirado removido: " + leader);
        }
    }

    public long getCurrentTerm() {
        IMap<String, Long> termMap = hazelcast.getMap(CURRENT_TERM_MAP);
        return termMap.getOrDefault(TERM_KEY, 0L);
    }

    public long incrementTerm() {
        IMap<String, Long> termMap = hazelcast.getMap(CURRENT_TERM_MAP);
        termMap.lock(TERM_LOCK_KEY);

        try{
        while (true) {
            long oldTerm = termMap.getOrDefault(TERM_KEY, 0L);
            long newTerm = oldTerm + 1;
            if (oldTerm == 0L) {
                if (termMap.putIfAbsent(TERM_KEY, newTerm) == null) {
                    System.out.println("⭐ Novo termo (CAS) definido: " + newTerm);
                    return newTerm;
                }
            } else {
                if (termMap.replace(TERM_KEY, oldTerm, newTerm)) {
                    System.out.println("⭐ Novo termo (CAS) definido: " + newTerm);
                    return newTerm;
                }
            }
        }
        } finally {
            termMap.unlock(TERM_LOCK_KEY);
        }
    }

    /**
     * Atualiza o índice do último log commitado deste nó.
     * Isso deve ser chamado sempre que o nó commitar uma nova operação.
     */
    public void updateLastCommittedIndex(long index) {
        IMap<String, Long> logMap = hazelcast.getMap(LOG_INDEX_MAP);
        String key = LOG_INDEX_KEY_PREFIX + nodeId;
        logMap.put(key, index);
    }

    /**
     * Obtém o índice do último log commitado de um nó específico.
     */
    public long getLastCommittedIndex(String targetNodeId) {
        IMap<String, Long> logMap = hazelcast.getMap(LOG_INDEX_MAP);
        String key = LOG_INDEX_KEY_PREFIX + targetNodeId;
        return logMap.getOrDefault(key, 0L);
    }

    /**
     * Obtém o índice do último log commitado deste nó.
     */
    public long getMyLastCommittedIndex() {
        return getLastCommittedIndex(this.nodeId);
    }

    /**
     * Coleta os índices de todos os nós do cluster para a eleição.
     */
    public Map<String, Long> getAllNodeLogIndices() {
        IMap<String, Long> logMap = hazelcast.getMap(LOG_INDEX_MAP);

        // Filtra apenas as chaves de log-index e retorna
        return logMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LOG_INDEX_KEY_PREFIX))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().replace(LOG_INDEX_KEY_PREFIX, ""),
                        Map.Entry::getValue
                ));
    }
}