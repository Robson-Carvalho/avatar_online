package com.avatar.avatar_online.DTOs;

import java.util.List;

public class PackResponseDto {
    private String player;
    private String transaction;
    private int newCardsFromPack;
    private List<CardNFTRequestDto> cartasDoPack;

    // getters e setters

    public PackResponseDto(){}

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public int getNewCardsFromPack() {
        return newCardsFromPack;
    }

    public void setNewCardsFromPack(int newCardsFromPack) {
        this.newCardsFromPack = newCardsFromPack;
    }

    public List<CardNFTRequestDto> getCartasDoPack() {
        return cartasDoPack;
    }

    public void setCartasDoPack(List<CardNFTRequestDto> cartasDoPack) {
        this.cartasDoPack = cartasDoPack;
    }
}
