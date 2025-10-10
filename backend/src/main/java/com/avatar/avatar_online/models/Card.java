package com.avatar.avatar_online.models;

import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.enums.PhaseCard;
import com.avatar.avatar_online.enums.RarityCard;
import jakarta.persistence.*;

import java.util.UUID;

@Entity (name  = "card")
@Table (name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // cria a FK
    private User user;

    private String name;
    @Enumerated(EnumType.STRING)
    private ElementCard element;

    @Enumerated(EnumType.STRING)
    private PhaseCard phase;
    private int attack;
    private int life;
    private int defense;

    @Enumerated(EnumType.STRING)
    private RarityCard rarity;
    private String description;

    public Card() {
    }

    public Card(User user, String name, ElementCard element, PhaseCard phase, int attack, int life, int defense, RarityCard rarity, String description) {
        this.user = user;
        this.name = name;
        this.element = element;
        this.phase = phase;
        this.attack = attack;
        this.life = life;
        this.defense = defense;
        this.rarity = rarity;
        this.description = description;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public User setUser(User user) {
        return this.user = user;
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

}