package com.avatar.avatar_online.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


@Entity
public class Deck {
    @Id
    private Long id;
    private String userId;
    private String card1Id;
    private String card2Id;
    private String card3Id;
    private String card4Id;
    private String card5Id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
