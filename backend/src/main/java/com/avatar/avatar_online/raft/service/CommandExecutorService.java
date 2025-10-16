package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.model.LogEntry;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.CardRepository;
import com.avatar.avatar_online.repository.DeckRepository;
import com.avatar.avatar_online.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandExecutorService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    public CommandExecutorService(UserRepository userRepository, DeckRepository deckRepository) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;

    }

    public void executeCommand(LogEntry entry) {
        Object command = entry.getCommand();

        System.out.println("⚡ Aplicando comando: " + command.getClass().getSimpleName() +
                " no índice " + entry.getIndex());

        if (command instanceof UserSignUpCommand) {
            applyUserSignUp((UserSignUpCommand) command);
        }
        // Adicionar outros comandos aqui:
        // else if (command instanceof SetDeckCommmand) {
        //     applySetDeckCommand((SetDeckCommmand) command);
        // }
        // ...
    }

    @Transactional
    public void applyUserSignUp(UserSignUpCommand command) {
        System.out.println("   -> Executando UserSignUp para: " + command.getEmail());
        applyUserSignUpCommand(command);
    }

    private void applyCard(){

    }

    // ... Métodos privados para aplicar outros comandos ...

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

        userRepository.save(newUser);
        deckRepository.save(newDeck);
    }
}