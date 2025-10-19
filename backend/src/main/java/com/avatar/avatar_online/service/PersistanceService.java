package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.DeckRepository;
import com.avatar.avatar_online.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistanceService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    public PersistanceService(UserRepository userRepository, DeckRepository deckRepository) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    @Transactional
    public void applyUserSignUpCommand(UserSignUpCommand command){
        User newUser = new User(
                command.getPlayerId(),
                command.getName(),
                command.getNickname(),
                command.getEmail(),
                command.getPassword()
        );

        Deck newDeck = new Deck();

        newDeck.setId(command.getDeckId());
        newUser.setId(command.getPlayerId());
        newDeck.setUser(newUser.getId());

        try {
            userRepository.save(newUser);
            deckRepository.save(newDeck);
        } catch (DataIntegrityViolationException e){
            if (e.getMessage().contains("users_pkey")) {
                System.out.println("Idempotência tratada: Comando já aplicado.");
                return;
            }
            throw e;
        }

    }
}
