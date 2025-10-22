package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;

import java.util.List;
import java.util.Optional;

public class GetDeckResponseDTO {
    private Optional<Deck> deck;
    private List<CardDTO> cards;

    public GetDeckResponseDTO(Optional<Deck> deck, List<CardDTO> cards) {
        this.deck = deck;
        this.cards = cards;
    }

    public  Optional<Deck> getDeck() {
        return deck;
    }

    public void setDeck(Optional<Deck> deck) {
        this.deck = deck;
    }

    public List<CardDTO> getCards() {
        return cards;
    }

    public void setCards(List<CardDTO> cards) {
        this.cards = cards;
    }
}
