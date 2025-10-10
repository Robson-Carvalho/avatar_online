package com.avatar.avatar_online.raft.Logs;

import java.util.List;
import java.util.UUID;

public class OpenPackCommand {

    private final UUID commandId;

    private final String operationType;

    private final String playerId;

    private final List<UUID> cardsIds;

    public OpenPackCommand(UUID commandId, String operationType, String playerId, List<UUID> cardsIds) {
        this.commandId = commandId;
        this.operationType = operationType;
        this.playerId = playerId;
        this.cardsIds = cardsIds;
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

    public List<UUID> getCardsIds() {
        return cardsIds;
    }
}
