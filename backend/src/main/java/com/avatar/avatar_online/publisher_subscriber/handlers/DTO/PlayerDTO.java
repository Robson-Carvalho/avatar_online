package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.game.Player;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerDTO implements Serializable {
    private final String id;
    private final int points;
    private final String activationCardId; // Apenas o ID
    private final Boolean playedCard;
    private final List<CardDTO> cards; // Lista de CardDTOs

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

    public PlayerDTO(Player player) {
        this.id = player.getId();
        this.points = player.getPoints();
        // Não envie o objeto Card, envie apenas o ID.
        // O método getActivationCard() retorna Card, o objeto Match/GameState não tem o ID.
        // Precisamos assumir que o ID da carta ativada está na propriedade 'activationCard' em Player.
        // Se a propriedade 'activationCard' é uma String com o ID da carta:
        this.activationCardId = player.getActivationCard() != null ? player.getActivationCard().getId().toString() : "";

        // Se precisar do activationCard diretamente do Player, você precisaria de um getter lá.
        // Assumindo que você usa o ID da carta:
        // this.activationCardId = player.getActivationCardId(); // Se existisse este getter

        this.playedCard = player.getPlayedCard();
        this.cards = player.getCards().stream()
                .map(CardDTO::new)
                .collect(Collectors.toList());
    }

    // Getters para serialização...
    public String getId() { return id; }
    public int getPoints() { return points; }
    public String getActivationCardId() { return activationCardId; }
    public Boolean getPlayedCard() { return playedCard; }
    public List<CardDTO> getCards() { return cards; }
}