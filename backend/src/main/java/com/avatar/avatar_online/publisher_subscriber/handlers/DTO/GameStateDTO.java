package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.game.GameState;

import java.io.Serializable;
import java.util.List;

public class GameStateDTO implements Serializable {
    private final String id;
    private final String turnPlayerId;
    private final String playerWin;
    private final PlayerDTO playerOne; // Usando PlayerDTO
    private final PlayerDTO playerTwo; // Usando PlayerDTO

    // ... adicione outros campos simples que o cliente precisa, como 'message' e 'type'

    public GameStateDTO(GameState gameState) {
        this.id = gameState.getId();
        this.turnPlayerId = gameState.getTurnPlayerId();
        this.playerWin = gameState.getPlayerWin();
        // Converte o Player do dominio para PlayerDTO
        this.playerOne = new PlayerDTO(gameState.getPlayerOne());
        this.playerTwo = new PlayerDTO(gameState.getPlayerTwo());
    }

    // Getters para serialização
    public String getId() { return id; }
    public String getTurnPlayerId() { return turnPlayerId; }
    public String getPlayerWin() { return playerWin; }
    public PlayerDTO getPlayerOne() { return playerOne; }
    public PlayerDTO getPlayerTwo() { return playerTwo; }
}