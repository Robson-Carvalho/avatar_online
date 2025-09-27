package com.avatar.avatar_online.models;

import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.enums.PhaseCard;
import com.avatar.avatar_online.enums.RarityCard;

import java.util.Map;
import java.util.UUID;

public class Card {
    private String id;
    private String userId;
    private String name;
    private ElementCard element;
    private PhaseCard phase;
    private int attack;
    private int life;
    private int defense;
    private RarityCard rarity;
    private String description;

    public Card() {
        this.id = UUID.randomUUID().toString();
    }

    public Card(String userId, String name, ElementCard element, PhaseCard phase, int attack, int life, int defense, RarityCard rarity, String description) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.element = element;
        this.phase = phase;
        this.attack = attack;
        this.life = life;
        this.defense = defense;
        this.rarity = rarity;
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String setUserId(String userId) {
        return this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ElementCard getElement() {
        return element;
    }

    public void setElement(ElementCard element) {
        this.element = element;
    }

    public PhaseCard getPhase() {
        return phase;
    }

    public void setPhase(PhaseCard phase) {
        this.phase = phase;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public RarityCard getRarity() {
        return rarity;
    }

    public void setRarity(RarityCard rarity) {
        this.rarity = rarity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Map<String, Object> toJson() {
        return Map.of(
                "id", this.id,
                "name", this.name,
                "userId", this.userId,
                "element", this.element,
                "phase", this.phase,
                "attack", this.attack,
                "life", this.life,
                "defense", this.defense,
                "rarity", this.rarity,
                "description", this.description
        );
    }
}