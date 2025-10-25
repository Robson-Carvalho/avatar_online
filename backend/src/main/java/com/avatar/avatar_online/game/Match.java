package com.avatar.avatar_online.game;

import com.avatar.avatar_online.publisher_subscriber.handlers.records.PlayerInGame;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Match implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String matchId;
    private String managerNodeId;
    private PlayerInGame player1;
    private PlayerInGame player2;
    private GameState gameState;

    public Match(){ }

    public Match(String managerNodeId, PlayerInGame player1, PlayerInGame player2, GameState gameState) {
        this.matchId = UUID.randomUUID().toString();
        this.managerNodeId = managerNodeId;
        this.player1 = player1;
        this.player2 = player2;
        this.gameState = gameState;
    }

    public void playCard(String userID){
        if(gameState.getPlayerOne().getId().equals(userID) && !Objects.equals(gameState.getPlayerOne().getActivationCard(), "")){
            gameState.getPlayerOne().setPlayedCard(true);
            gameState.setTurnPlayerId(gameState.getPlayerTwo().getId());
        }else if(gameState.getPlayerTwo().getId().equals(userID) && !Objects.equals(gameState.getPlayerTwo().getActivationCard(), "")) {
            gameState.getPlayerTwo().setPlayedCard(true);
            gameState.setTurnPlayerId(gameState.getPlayerOne().getId());
        }

        if(gameState.getPlayerOne().getPlayedCard() && gameState.getPlayerTwo().getPlayedCard()){
            gameState.battle();
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public PlayerInGame getPlayer1() {
        return player1;
    }

    public PlayerInGame getPlayer2() {
        return player2;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getManagerNodeId() {
        return managerNodeId;
    }

    public void setManagerNodeId(String managerNodeId) {
        this.managerNodeId = managerNodeId;
    }

    public boolean getIslocalMatch() {
        return getPlayer1().getHostAddress().equals(getPlayer2().getHostAddress());
    }

    public boolean isLocalMatch() {
        return Objects.equals(player1.getHostAddress(), player2.getHostAddress());
    }
}
