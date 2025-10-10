package com.avatar.avatar_online.raft.Logs;

import java.io.Serializable;
import java.util.UUID;

public class UserSignUpCommand implements Serializable {
    private final UUID commandId;

    private final String operationType;

    private final String playerId;

    private final String name;

    private final String email;

    private final String nickname;

    private final String password;

    public  UserSignUpCommand(UUID commandId, String operationType, String playerId, String name,
                              String email, String nickname, String password) {
        this.commandId = commandId;
        this.operationType = operationType;
        this.playerId = playerId;
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    public UUID getCommandId() {
        return commandId;
    }
    public String getOperationType() {
        return operationType;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }
}
