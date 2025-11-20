package com.avatar.avatar_online.raft.logs;

import com.avatar.avatar_online.models.Card;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class OpenPackCommand implements Serializable {

    private final UUID commandId;

    private final String operationType;

    private final UUID playerId;

    private List<Card> cards;

    public OpenPackCommand(UUID commandId, String operationType, UUID playerId, List<Card> cards) {
        this.commandId = commandId;
        this.operationType = operationType;
        this.playerId = playerId;
        this.cards = cards;
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

    public List<Card> getCards() { return cards; }

}
