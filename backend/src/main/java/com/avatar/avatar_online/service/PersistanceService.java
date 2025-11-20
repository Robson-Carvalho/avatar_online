package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.TradeCardsCommand;
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
    public void applyUserSignUpCommand(UserSignUpCommand command) {

        if (userRepository.existsById(command.getPlayerId())){
            System.out.println("Idempotência Raft: Comando de SignUp para usuário " + command.getPlayerId() + " já aplicado.");
            return;
        }

        User newUser = new User(
                command.getPlayerId(),
                command.getName(),
                command.getNickname(),
                command.getEmail(),
                command.getPassword(),
                command.getPrivateKey(),
                command.getAddress()
        );
        newUser.setId(command.getPlayerId());

        Deck newDeck = new Deck();
        newDeck.setId(command.getDeckId());
        newDeck.setUser(newUser.getId());

        userRepository.save(newUser);
        deckRepository.save(newDeck);
    }

    @Transactional
    public void applyOpenPackCommand(OpenPackCommand command){
        List<Card> cards = command.getCards();

        cardRepository.saveAll(cards);
    }

    @Transactional
    public void applySetDeckCommand(SetDeckCommmand command){
        Optional<Deck> deckOptional = deckRepository.findByUser(command.getUserId());

        if(deckOptional.isEmpty()){
            throw new RuntimeException("Deck não encontrado para o usuário: " + command.getUserId());
        }

        Deck deck = deckOptional.get();

        deck.setCard1(command.getCard1Id());
        deck.setCard2(command.getCard2Id());
        deck.setCard3(command.getCard3Id());
        deck.setCard4(command.getCard4Id());
        deck.setCard5(command.getCard5Id());

        deckRepository.save(deck);
    }

    @Transactional
    public void applyTradeCardCommand(TradeCardsCommand command){
        Optional<Card> card1 = cardRepository.findById(command.getCard1Id());
        Optional<Card> card2 = cardRepository.findById(command.getCard2Id());
        Optional<User> user1 = userRepository.findById(command.getPlayer1Id());
        Optional<User> user2 = userRepository.findById(command.getPlayer2Id());


        if(card1.isEmpty()){
            throw new RuntimeException("Carta 1 não encontrada: " + command.getCard1Id());
        }

        if(card2.isEmpty()){
            throw new RuntimeException("Carta 2 não encontrada: " + command.getCard2Id());
        }

        if(user1.isEmpty()){
            throw new RuntimeException("usuário 1 não encontrado: " + command.getPlayer1Id());
        }

        if(user2.isEmpty()){
            throw new RuntimeException("usuário 2 não encontrado: " + command.getPlayer2Id());
        }

        Card card1True = card1.get();
        Card card2True = card2.get();
        User user = user1.get();
        User uuser = user2.get();

        card1True.setUser(uuser);
        card2True.setUser(user);

        cardRepository.save(card1True);
        cardRepository.save(card2True);
    }
}
