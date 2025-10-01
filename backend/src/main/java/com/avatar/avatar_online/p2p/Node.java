package com.avatar.avatar_online.p2p;

public class Node {
    private final String nodeId;
    private final String baseURL;

    public Node(String nodeId, String baseURL) {
        this.nodeId = nodeId;
        this.baseURL = baseURL;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getBaseURL() {
        return baseURL;
    }
}
