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
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference// cria a FK
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id1")
    @JsonBackReference
    private Card card1;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id2")
    @JsonBackReference
    private Card card2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id3")
    @JsonBackReference
    private Card card3;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id4")
    @JsonBackReference
    private Card card4;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id5")
    @JsonBackReference
    private Card card5;

    public Deck() {
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

    public Card getCard5() {
        return card5;
    }

    public void setCard5(Card card5) {
        this.card5 = card5;
    }

    public Card getCard4() {
        return card4;
    }

    public void setCard4(Card card4) {
        this.card4 = card4;
    }

    public Card getCard3() {
        return card3;
    }

    public void setCard3(Card card3) {
        this.card3 = card3;
    }

    public Card getCard2() {
        return card2;
    }

    public void setCard2(Card card2) {
        this.card2 = card2;
    }

    public Card getCard1() {
        return card1;
    }

    public void setCard1(Card card1) {
        this.card1 = card1;
    }
}
