package com.avatar.avatar_online.DTOs;

public class SwapCardsDTO {
    private String addressPlayer1;
    private String card1;
    private String addressPlayer2;
    private String card2;
    private String transaction;

    public SwapCardsDTO() {}

    public String getAddressPlayer1() {
        return addressPlayer1;
    }

    public void setAddressPlayer1(String addressPlayer1) {
        this.addressPlayer1 = addressPlayer1;
    }

    public String getCard1() {
        return card1;
    }

    public void setCard1(String card1) {
        this.card1 = card1;
    }

    public String getAddressPlayer2() {
        return addressPlayer2;
    }

    public void setAddressPlayer2(String addressPlayer2) {
        this.addressPlayer2 = addressPlayer2;
    }

    public String getCard2() {
        return card2;
    }

    public void setCard2(String card2) {
        this.card2 = card2;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }
}