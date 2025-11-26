package com.avatar.avatar_online.DTOs;

public class MintCardsDTO {
    private String address;
    private String transaction;
    private MintedCardDTO card;

    public MintCardsDTO() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public MintedCardDTO getCard() {
        return card;
    }

    public void setCard(MintedCardDTO card) {
        this.card = card;
    }
}

