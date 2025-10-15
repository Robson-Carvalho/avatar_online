package com.avatar.avatar_online.raft.model;

public class LeaderInfo {
    private String nodeId;
    private String host;
    private int port;
    private long lastHeartbeat;
    private String httpAddress;
    private long term;

    public LeaderInfo(String nodeId, String host, int port, long term) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.lastHeartbeat = System.currentTimeMillis();
        this.httpAddress = "http://" + host + ":" + port;
        this.term = term;
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

    public long getTerm() { return term; }

    @Override
    public String toString() {
        return "LeaderInfo{nodeId='" + nodeId + "', term=" + term + ", host='" + host + "', port=" + port +
                ", httpAddress='" + httpAddress + "', lastHeartbeat=" + lastHeartbeat + "}";
    }
}
