package com.avatar.avatar_online.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity (name = "deck")
@Table (name = "deck")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference// cria a FK
    private User user;

    @ManyToMany
    @JoinTable(
            name = "deck_cards",
            joinColumns = @JoinColumn(name = "deck_id"),
            inverseJoinColumns = @JoinColumn(name = "card_id")
    )
    @JsonManagedReference
    private List<Card> cards = new ArrayList<>();

    public Deck() {
    }

    public List<Card> getCards() {
        return cards;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCards(List<Card> cards) { this.cards = cards; }

    public void addCard(Card card) {
        if (cards.size() >= 5) {
            throw new IllegalStateException("Deck já tem 5 cartas");
        }
        cards.add(card);
    }
}
