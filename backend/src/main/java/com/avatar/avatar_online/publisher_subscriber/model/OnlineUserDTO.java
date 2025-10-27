package com.avatar.avatar_online.publisher_subscriber.model;

public class OnlineUserDTO {
    private String id;
    private String nickname;
    private String sessionId;
    private String host;

    public OnlineUserDTO() {}

    public OnlineUserDTO(String id, String nickname,  String sessionId, String host) {
        this.id = id;
        this.nickname = nickname;
        this.sessionId = sessionId;
        this.host = host;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
