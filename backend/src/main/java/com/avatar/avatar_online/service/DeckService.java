package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.repository.DeckRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeckService {

    private final DeckRepository deckRepository;

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    public List<Deck> findAll() {
        return deckRepository.findAll();
    }

    public Optional<Deck> findByUserId(String id) {
        return deckRepository.findByUser(UUID.fromString(id));
    }
}
