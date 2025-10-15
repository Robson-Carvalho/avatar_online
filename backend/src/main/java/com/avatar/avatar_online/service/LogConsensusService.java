package com.avatar.avatar_online.service;

import com.avatar.avatar_online.raft.config.NodeIDConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogConsensusService {

    private final HazelcastInstance hazelcast;
    private final String nodeId;

    private static final String LOG_INDEX_MAP = "node-log-index";
    private static final String LOG_INDEX_KEY_PREFIX = "log-index-node-";

    @Autowired
    public LogConsensusService(HazelcastInstance hazelcast, NodeIDConfig nodeIDConfig) {
        this.hazelcast = hazelcast;
        this.nodeId = nodeIDConfig.getNodeId();
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
                        Map.Entry::getValue));
    }
}
