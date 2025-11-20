package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetCardsResponseDTO {
    private String player;
    private String transaction;
    private int newCardsFromPack;
    private List<CardDTO> cartasDoPack;

    // Getters e setters

    @JsonProperty("player")
    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    @JsonProperty("transaction")
    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    @JsonProperty("newCardsFromPack")
    public int getNewCardsFromPack() {
        return newCardsFromPack;
    }

    public void setNewCardsFromPack(int newCardsFromPack) {
        this.newCardsFromPack = newCardsFromPack;
    }

    @JsonProperty("cartasDoPack")
    public List<CardDTO> getCartasDoPack() {
        return cartasDoPack;
    }

    public void setCartasDoPack(List<CardDTO> cartasDoPack) {
        this.cartasDoPack = cartasDoPack;
    }
}

