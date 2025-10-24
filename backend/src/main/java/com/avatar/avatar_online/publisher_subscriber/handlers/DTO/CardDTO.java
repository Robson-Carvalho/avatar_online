package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.enums.PhaseCard;

import java.io.Serializable;
import java.util.UUID;

public class CardDTO implements Serializable {
    private UUID id;
    private String name;
    private ElementCard element;
    private PhaseCard phase;
    private int attack;
    private int life;
    private int defense;

    public CardDTO() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public ElementCard getElement() { return element; }
    public PhaseCard getPhase() { return phase; }
    public int getAttack() { return attack; }
    public int getLife() { return life; }
    public int getDefense() { return defense; }

    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setElement(ElementCard element) { this.element = element; }
    public void setPhase(PhaseCard phase) { this.phase = phase; }
    public void setAttack(int attack) { this.attack = attack; }
    public void setLife(int life) { this.life = life; }
    public void setDefense(int defense) { this.defense = defense; }
}
