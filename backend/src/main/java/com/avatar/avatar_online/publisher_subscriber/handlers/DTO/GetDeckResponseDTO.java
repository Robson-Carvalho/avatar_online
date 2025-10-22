package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.DTOs.CardDTO;

import java.util.List;

public class GetDeckResponseDTO {
    private List<CardDTO> deck;
    private List<CardDTO> cards;

    public GetDeckResponseDTO(List<CardDTO> deck, List<CardDTO> cards) {
        this.deck = deck;
        this.cards = cards;
    }

    public  List<CardDTO> getDeck() {
        return deck;
    }

    public void setDeck(List<CardDTO> deck) {
        this.deck = deck;
    }

    public List<CardDTO> getCards() {
        return cards;
    }

    public void setCards(List<CardDTO> cards) {
        this.cards = cards;
    }
}
