package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.game.GameState;
import com.avatar.avatar_online.DTOs.CardDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class GameStateDTO implements Serializable {
    private final String id;
    private final String turnPlayerId;
    private final String playerWin;
    private final PlayerDTO playerOne; // Usando PlayerDTO
    private final PlayerDTO playerTwo; // Usando PlayerDTO

    // ... adicione outros campos simples que o cliente precisa, como 'message' e 'type'


    @ JsonCreator
    public GameStateDTO(
            @JsonProperty("id") String id,
            @JsonProperty("turnPlayerId") String turnPlayerId,
            @JsonProperty("playerWin") String playerWin,
            @JsonProperty("playerOne") PlayerDTO playerOne,
            @JsonProperty("playerTwo") PlayerDTO playerTwo) {

        this.id = id;
        this.turnPlayerId = turnPlayerId;
        this.playerWin = playerWin;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
    }

    public GameStateDTO(GameState gameState, List<CardDTO> playerCards, List<CardDTO> opponentCards) {
        this.id = gameState.getId();
        this.turnPlayerId = gameState.getTurnPlayerId();
        this.playerWin = gameState.getPlayerWin();
        this.playerOne = new PlayerDTO(gameState.getPlayerOne(), playerCards);
        this.playerTwo = new PlayerDTO(gameState.getPlayerTwo(), opponentCards);
        this.playerOne.setCards(playerCards);
        this.playerOne.setCards(playerCards);
    }


    // Getters para serialização
    public String getId() { return id; }
    public String getTurnPlayerId() { return turnPlayerId; }
    public String getPlayerWin() { return playerWin; }
    public PlayerDTO getPlayerOne() { return playerOne; }
    public PlayerDTO getPlayerTwo() { return playerTwo; }
}