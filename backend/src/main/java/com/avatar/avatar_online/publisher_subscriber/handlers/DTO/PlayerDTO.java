package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.game.Player;
import com.avatar.avatar_online.DTOs.CardDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class PlayerDTO implements Serializable {
    private final String id;
    private final int points;
    private final String activationCardId;
    private final Boolean playedCard;
    private List<CardDTO> cards;

    @JsonCreator
    public PlayerDTO(
            @JsonProperty("id") String id,
            @JsonProperty("points") int points,
            @JsonProperty("activationCardId") String activationCardId,
            @JsonProperty("playedCard") Boolean playedCard,
            @JsonProperty("cards") List<CardDTO> cards) {

        this.id = id;
        this.points = points;
        this.activationCardId = activationCardId;
        this.playedCard = playedCard;
        this.cards = cards;
    }

    public PlayerDTO(Player player, List<CardDTO> cards) {
        this.id = player.getId();
        this.points = player.getPoints();
        this.activationCardId = player.getActivationCardToBattle() != null ? player.getActivationCardToBattle().getId().toString() : "";
        this.playedCard = player.getPlayedCard();
        this.cards = cards;
    }

    // Getters para serialização...
    public String getId() { return id; }
    public int getPoints() { return points; }
    public String getActivationCardId() { return activationCardId; }
    public Boolean getPlayedCard() { return playedCard; }
    public List<CardDTO> getCards() { return cards; }
    public void setCards(List<CardDTO> cards) {
        this.cards = cards;
    }
}