package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> findAll() {
        return cardRepository.findAll();
    }
}
