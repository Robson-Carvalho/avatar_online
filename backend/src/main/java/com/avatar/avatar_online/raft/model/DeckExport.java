package com.avatar.avatar_online.raft.model;

import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;

import java.util.Map;
import java.util.UUID;

public record DeckExport(
        UUID id,
        UUID userId,
        UUID card1,
        UUID card2,
        UUID card3,
        UUID card4,
        UUID card5
) {
    public static DeckExport fromEntity(Deck entity) {
        return new DeckExport(
                entity.getId(),
                entity.getUser(),
                entity.getCard1(),
                entity.getCard2(),
                entity.getCard3(),
                entity.getCard4(),
                entity.getCard5()
        );
    }

    public Deck toEntity(Map<UUID, User> userMap) {
        Deck deck = new Deck();
        deck.setId(this.id);

        if (this.userId != null) {
            User user = userMap.get(this.userId);
            if (user != null) {
                deck.setUser(this.userId);
            }
        }

        deck.setCard1(this.card1);
        deck.setCard2(this.card2);
        deck.setCard3(this.card3);
        deck.setCard4(this.card4);
        deck.setCard5(this.card5);

        return deck;
    }
}