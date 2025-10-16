package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.config.NodeIDConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogConsensusService {

    private final HazelcastInstance hazelcast;
    private final String nodeId;

    private final IMap<String, Long> logMap;

    private static final String LOG_INDEX_MAP = "node-log-index";
    private static final String LOG_INDEX_KEY_PREFIX = "log-index-node-";

    @Autowired
    public LogConsensusService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast, NodeIDConfig nodeIDConfig) {
        this.hazelcast = hazelcast;
        this.nodeId = nodeIDConfig.getNodeId();
        this.logMap = hazelcast.getMap(LOG_INDEX_MAP);
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
}
