package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.CardRepository;
import com.avatar.avatar_online.repository.DeckRepository;
import com.avatar.avatar_online.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PersistanceService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;

    public PersistanceService(UserRepository userRepository, DeckRepository deckRepository,
                              CardRepository cardRepository) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
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

    @Transactional
    public void applyOpenPackCommand(OpenPackCommand command){
        UUID userId = command.getPlayerId();
        List<UUID> cardsIds = command.getCards();

        Optional<User> userOptional = userRepository.findById(userId);

        if(userOptional.isEmpty()){
            throw new RuntimeException("usuário não encontrado com ID: " + userId);
        }

        User user = userOptional.get();

        List<Card> cardsToAssign = cardRepository.findAllById(cardsIds);

        for (Card card : cardsToAssign) {
            card.setUser(user);
        }

        cardRepository.saveAll(cardsToAssign);
    }
}
