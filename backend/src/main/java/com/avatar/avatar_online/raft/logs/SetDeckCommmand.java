package com.avatar.avatar_online.raft.logs;

import java.io.Serializable;
import java.util.UUID;

public class SetDeckCommmand implements Serializable {

    private final UUID commandId;

    private final UUID userId;

    private final String operationType;

    private final UUID card1Id;
    private final UUID card2Id;
    private final UUID card3Id;
    private final UUID card4Id;
    private final UUID card5Id;

    public SetDeckCommmand(UUID commandId, UUID userId, String operationType, UUID card1Id,
                           UUID card2Id, UUID card3Id, UUID card4Id, UUID card5Id) {
        this.commandId = commandId;
        this.userId = userId;
        this.operationType = operationType;
        this.card1Id = card1Id;
        this.card2Id = card2Id;
        this.card3Id = card3Id;
        this.card4Id = card4Id;
        this.card5Id = card5Id;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getOperationType() {
        return operationType;
    }

    public UUID getCard1Id() {
        return card1Id;
    }

    public UUID getCard2Id() {
        return card2Id;
    }

    public UUID getCard3Id() {
        return card3Id;
    }

    public UUID getCard4Id() {
        return card4Id;
    }

    public UUID getCard5Id() {
        return card5Id;
    }
}
