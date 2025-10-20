package com.avatar.avatar_online.raft.logs;

import java.io.Serializable;
import java.util.UUID;

public class OpenPackCommand implements Serializable {

    private final UUID commandId;

    private final String operationType;

    private final UUID playerId;

    public OpenPackCommand(UUID commandId, String operationType, UUID playerId) {
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

    public UUID getPlayerId() {
        return playerId;
    }
}
