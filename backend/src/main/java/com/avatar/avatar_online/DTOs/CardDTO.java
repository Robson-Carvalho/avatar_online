package com.avatar.avatar_online.DTOs;

import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.enums.PhaseCard;
import com.avatar.avatar_online.enums.RarityCard;
import com.avatar.avatar_online.models.Card;

import java.util.UUID;

public class CardDTO {
    private UUID id;
    private String name;
    private ElementCard element;
    private PhaseCard phase;
    private int attack;
    private int life;
    private int defense;
    private RarityCard rarity;
    private String description;

    public CardDTO(Card card){
        this.id = card.getId();
        this.name = card.getName();
        this.element = card.getElement();
        this.phase = card.getPhase();
        this.attack = card.getAttack();
        this.life = card.getLife();
        this.defense = card.getDefense();
        this.rarity = card.getRarity();
        this.description = card.getDescription();
    }

    // getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public ElementCard getElement() { return element; }
    public PhaseCard getPhase() { return phase; }
    public int getAttack() { return attack; }
    public int getLife() { return life; }
    public int getDefense() { return defense; }
    public RarityCard getRarity() { return rarity; }
    public String getDescription() { return description; }
}
