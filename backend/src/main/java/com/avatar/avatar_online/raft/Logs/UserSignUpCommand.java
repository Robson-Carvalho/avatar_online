package com.avatar.avatar_online.raft.Logs;

import java.io.Serializable;
import java.util.UUID;

public class UserSignUpCommand implements Serializable {
    private final UUID commandId;

    private final String operationType;

    private final String playerId;

    public  UserSignUpCommand(UUID commandId, String operationType, String playerId) {
        this.commandId = commandId;
        this.operationType = operationType;
        this.playerId = playerId;
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
}
