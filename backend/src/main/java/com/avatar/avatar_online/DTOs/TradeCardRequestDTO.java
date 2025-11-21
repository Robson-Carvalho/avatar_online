package com.avatar.avatar_online.DTOs;

public class TradeCardRequestDTO {

    private String player1;
    private String cardId1;
    private String player2;
    private String cardId2;

    public TradeCardRequestDTO(String player1, String cardId1, String player2, String cardId2) {
        this.player1 = player1;
        this.cardId1 = cardId1;
        this.player2 = player2;
        this.cardId2 = cardId2;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getCardId1() {
        return cardId1;
    }

    public void setCardId1(String cardId1) {
        this.cardId1 = cardId1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getCardId2() {
        return cardId2;
    }

    public void setCardId2(String cardId2) {
        this.cardId2 = cardId2;
    }
}
