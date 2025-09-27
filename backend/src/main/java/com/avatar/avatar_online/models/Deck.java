package com.avatar.avatar_online.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity (name = "deck")
@Table (name = "deck")
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String userId;
    private String card1Id;
    private String card2Id;
    private String card3Id;
    private String card4Id;
    private String card5Id;

    public Deck() {
        this.id = UUID.randomUUID().toString();
    }

    public Deck(String userId) {
        this.id = userId;
    }

    public Deck(String userId, String card1Id, String card2Id, String card3Id, String card4Id, String card5Id) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.card1Id = card1Id;
        this.card2Id = card2Id;
        this.card3Id = card3Id;
        this.card4Id = card4Id;
        this.card5Id = card5Id;
    }

    public List<String> getCards() {
        List<String> cards = new ArrayList<>();

        if (card1Id != null && !card1Id.isEmpty()) cards.add(card1Id);
        if (card2Id != null && !card2Id.isEmpty()) cards.add(card2Id);
        if (card3Id != null && !card3Id.isEmpty()) cards.add(card3Id);
        if (card4Id != null && !card4Id.isEmpty()) cards.add(card4Id);
        if (card5Id != null && !card5Id.isEmpty()) cards.add(card5Id);

        return cards;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCard1Id() {
        return card1Id;
    }

    public void setCard1Id(String card1Id) {
        this.card1Id = card1Id;
    }

    public String getCard2Id() {
        return card2Id;
    }

    public void setCard2Id(String card2Id) {
        this.card2Id = card2Id;
    }

    public String getCard3Id() {
        return card3Id;
    }

    public void setCard3Id(String card3Id) {
        this.card3Id = card3Id;
    }

    public String getCard4Id() {
        return card4Id;
    }

    public void setCard4Id(String card4Id) {
        this.card4Id = card4Id;
    }

    public String getCard5Id() {
        return card5Id;
    }

    public void setCard5Id(String card5Id) {
        this.card5Id = card5Id;
    }
}
