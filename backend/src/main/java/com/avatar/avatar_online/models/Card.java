package com.avatar.avatar_online.models;

import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.enums.PhaseCard;
import com.avatar.avatar_online.enums.RarityCard;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "cards")
public class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ElementCard element;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhaseCard phase;

    @Column(nullable = false)
    private int attack;

    @Column(nullable = false)
    private int life;

    @Column(nullable = false)
    private int defense;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RarityCard rarity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String TokenId;

    // ===== Construtores =====
    public Card() {
        // O ID será gerado automaticamente
    }

    public Card(User user, String name, ElementCard element, PhaseCard phase,
                int attack, int life, int defense, RarityCard rarity, String description, String TokenId) {
        this.user = user;
        this.name = name;
        this.element = element;
        this.phase = phase;
        this.attack = attack;
        this.life = life;
        this.defense = defense;
        this.rarity = rarity;
        this.description = description;
        this.TokenId = TokenId;
    }

    public Card(Card card) {
    }

    // ===== Getters e Setters =====
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

    public String getTokenId() {
        return TokenId;
    }

    public void setTokenId(String tokenId) {
        TokenId = tokenId;
    }
}
