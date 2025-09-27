package com.avatar.avatar_online.models;

import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.enums.PhaseCard;
import com.avatar.avatar_online.enums.RarityCard;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SystemCard {
    @Id
    private Long id;
    private String name;
    private ElementCard element;
    private PhaseCard phase;
    private int attack;
    private int life;
    private int defense;
    private RarityCard rarity;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}