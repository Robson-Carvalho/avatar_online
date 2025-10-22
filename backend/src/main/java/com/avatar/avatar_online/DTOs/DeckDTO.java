package com.avatar.avatar_online.DTOs;

import java.util.UUID;

public class DeckDTO {
    private UUID userId;
    private UUID card1Id;
    private UUID card2Id;
    private UUID card3Id;
    private UUID card4Id;
    private UUID card5Id;

    public DeckDTO(){}

    public DeckDTO(UUID userId, UUID card1Id, UUID card2Id, UUID card3Id, UUID card4Id, UUID card5Id) {
        this.userId = userId;
        this.card1Id = card1Id;
        this.card2Id = card2Id;
        this.card3Id = card3Id;
        this.card4Id = card4Id;
        this.card5Id = card5Id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCard1Id() {
        return card1Id;
    }

    public void setCard1Id(UUID card1Id) {
        this.card1Id = card1Id;
    }

    public UUID getCard2Id() {
        return card2Id;
    }

    public void setCard2Id(UUID card2Id) {
        this.card2Id = card2Id;
    }

    public UUID getCard3Id() {
        return card3Id;
    }

    public void setCard3Id(UUID card3Id) {
        this.card3Id = card3Id;
    }

    public UUID getCard4Id() {
        return card4Id;
    }

    public void setCard4Id(UUID card4Id) {
        this.card4Id = card4Id;
    }

    public UUID getCard5Id() {
        return card5Id;
    }

    public void setCard5Id(UUID card5Id) {
        this.card5Id = card5Id;
    }
}
