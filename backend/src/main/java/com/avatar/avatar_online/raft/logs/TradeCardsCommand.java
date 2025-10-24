package com.avatar.avatar_online.raft.logs;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class TradeCardsCommand implements Serializable {

    private final UUID commandId;

    private final String operationType;

    private final UUID player1Id;

    private final UUID player2Id;

    private final UUID Card1Id;

    private final UUID Card2Id;

    public TradeCardsCommand(UUID commandId, String operationType, UUID player1Id, UUID player2Id, UUID card1Id, UUID card2Id) {
        this.commandId = commandId;
        this.operationType = operationType;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        Card1Id = card1Id;
        Card2Id = card2Id;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public String getOperationType() {
        return operationType;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public UUID getCard1Id() {
        return Card1Id;
    }

    public UUID getCard2Id() {
        return Card2Id;
    }
}
