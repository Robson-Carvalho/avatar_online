package com.avatar.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity()
@Table(name = "deck")
public class Deck {
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID user;

    @Column(name = "card_id1")
    private UUID card1;

    @Column(name = "card_id2")
    private UUID card2;

    @Column(name = "card_id3")
    private UUID card3;

    @Column(name = "card_id4")
    private UUID card4;

    @Column(name = "card_id5")
    private UUID card5;

    public Deck() {}

    // ===== Getters e Setters =====

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    public UUID getCard1() {
        return card1;
    }

    public void setCard1(UUID card1) {
        this.card1 = card1;
    }

    public UUID getCard2() {
        return card2;
    }

    public void setCard2(UUID card2) {
        this.card2 = card2;
    }

    public UUID getCard3() {
        return card3;
    }

    public void setCard3(UUID card3) {
        this.card3 = card3;
    }

    public UUID getCard4() {
        return card4;
    }

    public void setCard4(UUID card4) {
        this.card4 = card4;
    }

    public UUID getCard5() {
        return card5;
    }

    public void setCard5(UUID card5) {
        this.card5 = card5;
    }
}
