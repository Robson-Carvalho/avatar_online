package com.avatar.avatar_online.publisher_subscriber.model;

public class OnlineUserInfo {
    private String sessionId;
    private String host;

    public OnlineUserInfo(String sessionId, String host) {
        this.sessionId = sessionId;
        this.host = host;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
