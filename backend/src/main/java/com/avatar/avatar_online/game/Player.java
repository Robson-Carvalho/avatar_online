package com.avatar.avatar_online.game;

import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.models.Card;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String id;
    private int points;
    private String activationCard;
    private final List<String> cemetery;
    private List<Card> cards;
    private Boolean playedCard;

    public Player(String id) {
        this.id = id;
        this.points = 0;
        this.activationCard = "";
        this.cemetery = new ArrayList<>();
        this.playedCard = false;
        this.cards = new ArrayList<>();
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Player(String id, List<Card> cards)  {
        this.id = id;
        this.points = 0;
        this.activationCard = "";
        this.cemetery = new ArrayList<>();
        this.playedCard = false;
        this.cards = cards;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Boolean getPlayedCard() { return playedCard; }

    public void setPlayedCard(Boolean playedCard) {
        this.playedCard = playedCard;

        if(this.activationCard.isEmpty()){
            for(Card card : this.cards){
                if(card.getLife() > 0) {
                    this.activationCard = String.valueOf(card.getId());
                    return;
                }
            }
        }
    }

    public int getPoints() { return points; }

    public void addPoint() { this.points++; }

    public Card getActivationCard() {
        for (Card card : this.cards) {
            if(card.getId().equals(this.activationCard))
                return card;
        }

        return null;
    }

    public void setActivationCard(String cardID) {
        if(cemetery.contains(cardID)){
            return;
        }

        for(Card c : cards){
            if(c.getId().equals(cardID)){
                this.activationCard = String.valueOf(c.getId());
            }
        }
    }

    public List<Card> getCards() { return cards; }

    public Boolean reduceLifeCard(int attack, String attackElementStr) {
        Card card = this.getActivationCard();

        ElementCard attackElement = ElementCard.valueOf(attackElementStr.toUpperCase());
        ElementCard defenseElement = card.getElement();

        boolean hasAdvantage = switch (attackElement) {
            case WATER -> defenseElement == ElementCard.FIRE || defenseElement == ElementCard.LIGHTNING;
            case FIRE -> defenseElement == ElementCard.AIR || defenseElement == ElementCard.METAL;
            case EARTH -> defenseElement == ElementCard.LIGHTNING || defenseElement == ElementCard.FIRE;
            case AIR -> defenseElement == ElementCard.EARTH;
            case BLOOD -> defenseElement != ElementCard.AVATAR && defenseElement != ElementCard.BLOOD;
            case METAL -> defenseElement == ElementCard.EARTH || defenseElement == ElementCard.LIGHTNING;
            case LIGHTNING -> defenseElement == ElementCard.WATER || defenseElement == ElementCard.AIR;
            case AVATAR -> true;
        };

        int effectiveDefense = hasAdvantage ? 0 : card.getDefense();
        int damage = Math.max(attack - effectiveDefense, 0);
        int newLife = card.getLife() - damage;

        if (newLife <= 0) {
            card.setLife(0);
            this.cemetery.add(card.getId().toString());
            this.activationCard = "";
        } else {
            card.setLife(newLife);
        }

        return newLife <= 0;
    }
}