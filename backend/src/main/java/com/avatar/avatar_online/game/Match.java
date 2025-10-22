package com.avatar.avatar_online.game;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Match implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String matchId;

    private String managerNodeId;

    private String player1NodeId;
    private String player1SessionId;

    private String player2NodeId;
    private String player2SessionId;

    public Match() {}

    public Match(String matchId, String managerNodeId, String player1NodeId, String player1SessionId, String player2NodeId, String player2SessionId) {
        this.matchId = matchId;
        this.managerNodeId = managerNodeId;
        this.player1NodeId = player1NodeId;
        this.player1SessionId = player1SessionId;
        this.player2NodeId = player2NodeId;
        this.player2SessionId = player2SessionId;
    }

    // --- Getters e Setters ---

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

    public String getPlayer1NodeId() {
        return player1NodeId;
    }

    public void setPlayer1NodeId(String player1NodeId) {
        this.player1NodeId = player1NodeId;
    }

    public String getPlayer1SessionId() {
        return player1SessionId;
    }

    public void setPlayer1SessionId(String player1SessionId) {
        this.player1SessionId = player1SessionId;
    }

    public String getPlayer2NodeId() {
        return player2NodeId;
    }

    public void setPlayer2NodeId(String player2NodeId) {
        this.player2NodeId = player2NodeId;
    }

    public String getPlayer2SessionId() {
        return player2SessionId;
    }

    public void setPlayer2SessionId(String player2SessionId) {
        this.player2SessionId = player2SessionId;
    }

    public boolean isLocalMatch() {
        return Objects.equals(player1NodeId, player2NodeId);
    }
}
