package com.avatar.avatar_online.publisher_subscriber.handlers.records;

import java.io.Serial;
import java.io.Serializable;

public class PlayerInGame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String userId; //
    private String userSession;
    private String hostAddress; //

    public PlayerInGame(String userId,  String userSession, String hostAddress) {
        this.userId = userId;
        this.userSession = userSession;
        this.hostAddress = hostAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserSession() {
        return userSession;
    }

    public void setUserSession(String userSession) {
        this.userSession = userSession;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }
}