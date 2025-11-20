package com.avatar.avatar_online.Utils;

import com.avatar.avatar_online.DTOs.CardNFTRequestDto;
import com.avatar.avatar_online.enums.ElementCard;
import com.avatar.avatar_online.enums.PhaseCard;
import com.avatar.avatar_online.enums.RarityCard;
import com.avatar.avatar_online.models.Card;

import java.util.UUID;

public class CardMapper {

    public static Card fromNFT(CardNFTRequestDto dto) {

        Card card = new Card();

        card.setId(UUID.randomUUID());
        card.setName(dto.getName());
        card.setElement(ElementCard.valueOf(dto.getElement()));
        card.setPhase(PhaseCard.valueOf(dto.getPhase()));
        card.setRarity(RarityCard.valueOf(dto.getRarity()));
        card.setAttack(Integer.parseInt(dto.getAttack()));
        card.setLife(Integer.parseInt(dto.getLife()));
        card.setDefense(Integer.parseInt(dto.getDefense()));
        card.setDescription(dto.getDescription());
        card.setTokenId(dto.getId());

        return card;
    }

}
