package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.config.NodeIDConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LogConsensusService {

    private final HazelcastInstance hazelcast;
    private final String nodeId;

    private final IMap<String, Long> logMap;

    private static final String LOG_INDEX_MAP = "node-log-index";
    private static final String LOG_INDEX_KEY_PREFIX = "log-index-node-";

    // Mapa de Logs Finais (para a eleição - OBRIGATÓRIO)
    private final IMap<String, Long> logEndMap;
    private static final String LOG_END_INDEX_MAP = "node-log-end-index";
    private static final String LOG_END_KEY_PREFIX = "log-end-index-node-";

    private final Map<UUID, Long> matchIndex = new ConcurrentHashMap<>();
    private final Map<UUID, Long> nextIndex = new ConcurrentHashMap<>();

    @Autowired
    public LogConsensusService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast, NodeIDConfig nodeIDConfig) {
        this.hazelcast = hazelcast;
        this.nodeId = nodeIDConfig.getNodeId();
        this.logMap = hazelcast.getMap(LOG_INDEX_MAP);
        this.logEndMap = hazelcast.getMap(LOG_END_INDEX_MAP);
    }

    /**
     * Atualiza o índice do último log commitado deste nó.
     * Isso deve ser chamado sempre que o nó commitar uma nova operação.
     */
    public void updateLastCommittedIndex(long index) {
        String key = LOG_INDEX_KEY_PREFIX + nodeId;
        this.logMap.put(key, index);
    }

    /**
     * Obtém o índice do último log commitado de um nó específico.
     */
    public long getLastCommittedIndex(String targetNodeId) {
        String key = LOG_INDEX_KEY_PREFIX + targetNodeId;
        return this.logMap.getOrDefault(key, 0L);
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
                        Map.Entry::getValue));
    }

    /**
     * Atualiza o índice do último log persistido (Log End) deste nó.
     * Deve ser chamado sempre que uma LogEntry for anexada/replicada.
     */
    public void updateLastLogIndex(long index) {
        String key = LOG_END_KEY_PREFIX + nodeId;
        this.logEndMap.put(key, index);
    }

    /**
     * Coleta os índices dos últimos logs PERSISTIDOS de todos os nós para a eleição.
     */
    public Map<String, Long> getAllNodeLastLogIndices() {
        // Usa o NOVO mapa, logEndMap
        return logEndMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LOG_END_KEY_PREFIX))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().replace(LOG_END_KEY_PREFIX, ""),
                        Map.Entry::getValue));
    }

    // --- Métodos para Reparo de LOG ---

    /**
     * Obtém o próximo índice para o Seguidor. Se não existir, retorna 1.
     */
    public long getNextIndex(UUID followerUuid) {
        return nextIndex.getOrDefault(followerUuid, 1L);
    }

    /**
     * 🚨 CHAMADO QUANDO HÁ FALHA DE logMismatch. Decrementa o nextIndex em 1.
     */
    public void decrementNextIndex(UUID followerUuid) {
        nextIndex.computeIfPresent(followerUuid, (k, v) -> Math.max(1, v - 1));
    }

    /**
     * CHAMADO APÓS SUCESSO DO APPENDENTRIES.
     */
    public void updateIndexesOnSuccess(UUID followerUuid, long newMatchIndex) {
        matchIndex.put(followerUuid, newMatchIndex);
        nextIndex.put(followerUuid, newMatchIndex + 1);
    }

    public void initializeLeaderState(long lastLogIndex) {
        long initialNextIndex = lastLogIndex + 1;

        // Pega todos os membros do cluster (exceto o próprio líder)
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .map(member -> member.getUuid()) // Se estiver usando UUID como chave
                .forEach(followerUuid -> {
                    nextIndex.put(followerUuid, initialNextIndex);
                    matchIndex.put(followerUuid, 0L); // matchIndex começa em 0
                    System.out.println("-> Inicializando nextIndex para " + followerUuid + " em: " + initialNextIndex);
                });
    }
}
