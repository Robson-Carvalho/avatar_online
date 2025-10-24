package com.avatar.avatar_online.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TradeCardDTO {
    @JsonProperty("PLayerId1")
    private String playerId1;

    @JsonProperty("PLayerId2")
    private String playerId2;

    @JsonProperty("CardId1")
    private String cardId1;

    @JsonProperty("CardId2")
    private String cardId2;

    public TradeCardDTO() {}

    public TradeCardDTO(String PLayerId1, String PLayerId2, String CardId1, String CardId2) {
        this.playerId1 = PLayerId1;
        this.playerId2 = PLayerId2;
        this.cardId1 = CardId1;
        this.cardId2 = CardId2;
    }

    public String getPLayerId1() {
        return playerId1;
    }

    public void setPLayerId1(String PLayerId1) {
        this.playerId1 = PLayerId1;
    }

    public String getPLayerId2() {
        return playerId2;
    }

    public void setPLayerId2(String PLayerId2) {
        this.playerId2 = PLayerId2;
    }

    public String getCardId1() {
        return cardId1;
    }

    public void setCardId1(String cardId1) {
        this.cardId1 = cardId1;
    }

    public String getCardId2() {
        return cardId2;
    }

    public void setCardId2(String cardId2) {
        this.cardId2 = cardId2;
    }
}
