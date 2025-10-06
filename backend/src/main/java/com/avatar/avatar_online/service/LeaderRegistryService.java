package com.avatar.avatar_online.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LeaderRegistryService {

    private final HazelcastInstance hazelcast;

    @Value("${app.node.id:node-1}")
    private String nodeId;

    @Value("${app.server.port:8080}")
    private int serverPort;

    private ScheduledExecutorService heartbeatScheduler;
    private static final String LEADER_REGISTRY_MAP = "leader-registry";
    private static final String LEADER_KEY = "current-leader";

    // Estrutura para armazenar informações do líder
    public static class LeaderInfo {
        private String nodeId;
        private String host;
        private int port;
        private long lastHeartbeat;
        private String httpAddress;

        public LeaderInfo() {}

        public LeaderInfo(String nodeId, String host, int port) {
            this.nodeId = nodeId;
            this.host = host;
            this.port = port;
            this.lastHeartbeat = System.currentTimeMillis();
            this.httpAddress = "http://" + host + ":" + port;
        }

        // Getters e Setters
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public long getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

        public String getHttpAddress() { return httpAddress; }
        public void setHttpAddress(String httpAddress) { this.httpAddress = httpAddress; }

        public void updateHeartbeat() {
            this.lastHeartbeat = System.currentTimeMillis();
        }

        public boolean isExpired(long timeoutMs) {
            return (System.currentTimeMillis() - lastHeartbeat) > timeoutMs;
        }

        @Override
        public String toString() {
            return "LeaderInfo{nodeId='" + nodeId + "', host='" + host + "', port=" + port +
                    ", httpAddress='" + httpAddress + "', lastHeartbeat=" + lastHeartbeat + "}";
        }
    }

    public LeaderRegistryService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @PostConstruct
    public void init() {
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Registra este nó como líder no cluster
     */
    public void registerAsLeader() {
        String currentHost = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        LeaderInfo leaderInfo = new LeaderInfo(nodeId, currentHost, serverPort);

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

        if (currentLeader != null && currentLeader.nodeId.equals(nodeId)) {
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
        return leader != null && leader.nodeId.equals(nodeId);
    }

    /**
     * Atualiza heartbeat do líder
     */
    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                LeaderInfo currentLeader = getCurrentLeader();
                if (currentLeader != null && currentLeader.nodeId.equals(nodeId)) {
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
}