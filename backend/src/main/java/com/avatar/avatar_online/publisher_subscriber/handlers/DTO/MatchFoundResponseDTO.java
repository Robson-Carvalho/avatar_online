package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.publisher_subscriber.handlers.records.PlayerInGame;

import java.util.UUID;
import java.io.Serializable;

// Este DTO substitui o Map que você estava usando
public class MatchFoundResponseDTO implements Serializable {
    private String matchId;
    private String managerNodeId;
    private GameStateDTO gameState; // Usará um DTO para o GameState também!
    private PlayerInGame player1;
    private PlayerInGame player2;

    public MatchFoundResponseDTO() {}

    // Construtor que recebe as informações do domínio
    public MatchFoundResponseDTO(String matchId, String managerNodeId, GameStateDTO gameState,
                                 PlayerInGame player1, PlayerInGame player2) {
        this.matchId = matchId;
        this.managerNodeId = managerNodeId;
        this.gameState = gameState;
        this.player1 = player1;
        this.player2 = player2;
    }

    // Getters e Setters (Obrigatorios para a serialização)
    public String getManagerNodeId() {
        return this.managerNodeId;
    }

    public void setManagerNodeId(String managerNodeId) {
        this.managerNodeId = managerNodeId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public GameStateDTO getGameState() {
        return gameState;
    }

    public void setGameState(GameStateDTO gameState) {
        this.gameState = gameState;
    }

    public PlayerInGame getPlayer1() {
        return player1;
    }

    public void setPlayer1(PlayerInGame player1) {
        this.player1 = player1;
    }

    public PlayerInGame getPlayer2() {
        return player2;
    }

    public void setPlayer2(PlayerInGame player2) {
        this.player2 = player2;
    }
}