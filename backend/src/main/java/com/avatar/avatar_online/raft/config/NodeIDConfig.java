package com.avatar.avatar_online.raft.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NodeIDConfig {

    private final String nodeId;

    public NodeIDConfig(@Value("${app.node.id:}") String configuredNodeId) {
        if (configuredNodeId != null && !configuredNodeId.trim().isEmpty() && !configuredNodeId.equals("${app.node.id:}")) {
            this.nodeId = configuredNodeId;
        } else {
            this.nodeId = "node-" + java.util.UUID.randomUUID();
        }
    }

    public String getNodeId() {
        return nodeId;
    }
}