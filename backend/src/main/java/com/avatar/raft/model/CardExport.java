package com.avatar.raft.model;

import com.avatar.models.Card;
import com.avatar.enums.ElementCard; // 🔑 Importar Enums
import com.avatar.enums.PhaseCard;   // 🔑 Importar Enums
import com.avatar.enums.RarityCard;  // 🔑 Importar Enums
import com.avatar.models.User;

import java.util.Map;
import java.util.UUID;

public record CardExport(
        UUID id,
        String name,
        UUID userId,
        ElementCard element,
        PhaseCard phase,
        int attack,
        int life,
        int defense,
        RarityCard rarity,
        String description
) {
    public static CardExport fromEntity(Card entity) {
        return new CardExport(
                entity.getId(),
                entity.getName(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getElement(),
                entity.getPhase(),
                entity.getAttack(),
                entity.getLife(),
                entity.getDefense(),
                entity.getRarity(),
                entity.getDescription()
        );
    }

    public Card toEntity(Map<UUID, User> userMap) {
        Card card = new Card();
        card.setId(this.id);
        card.setName(this.name);

        card.setElement(this.element);
        card.setPhase(this.phase);
        card.setAttack(this.attack);
        card.setLife(this.life);
        card.setDefense(this.defense);
        card.setRarity(this.rarity);
        card.setDescription(this.description);

        if (this.userId != null) {
            User user = userMap.get(this.userId);
            if (user != null) {
                card.setUser(user);
            } else {
                System.err.println("Aviso: Usuário de ID " + this.userId + " não encontrado para a carta " + this.id);
            }
        } else {
            card.setUser(null);
        }
        return card;
    }
}