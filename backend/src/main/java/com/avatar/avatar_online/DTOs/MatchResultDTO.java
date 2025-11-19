package com.avatar.avatar_online.DTOs;

public class MatchResultDTO {

    private String player1;
    private String player2;
    private String winner;

    public MatchResultDTO(String player1, String player2, String winner) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
    }

    public MatchResultDTO() {}

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}
