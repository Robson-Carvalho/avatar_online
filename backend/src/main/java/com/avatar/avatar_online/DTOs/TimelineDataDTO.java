package com.avatar.avatar_online.DTOs;

import java.util.List;

public class TimelineDataDTO {
    private String address;
    private String transaction;
    private String packId;
    private List<String> cards; // usado no open_package
    private MintedCardDTO card; // usado no mint_cards

    public TimelineDataDTO() {}

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

    public String getPackId() {
        return packId;
    }

    public void setPackId(String packId) {
        this.packId = packId;
    }

    public List<String> getCards() {
        return cards;
    }

    public void setCards(List<String> cards) {
        this.cards = cards;
    }

    public MintedCardDTO getCard() {
        return card;
    }

    public void setCard(MintedCardDTO card) {
        this.card = card;
    }
}
