package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import java.util.UUID;
import java.io.Serializable;

// Este DTO substitui o Map que você estava usando
public class MatchFoundResponseDTO implements Serializable {
    private String matchId;
    private String managerNodeId;
    private GameStateDTO gameState; // Usará um DTO para o GameState também!

    // Construtor que recebe as informações do domínio
    public MatchFoundResponseDTO(String matchId, String managerNodeId, GameStateDTO gameState) {
        this.matchId = matchId;
        this.managerNodeId = managerNodeId;
        this.gameState = gameState;
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
}