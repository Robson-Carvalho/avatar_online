package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.model.LogEntry;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.CardRepository;
import com.avatar.avatar_online.repository.DeckRepository;
import com.avatar.avatar_online.repository.UserRepository;
import com.avatar.avatar_online.service.PersistanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandExecutorService {

    private final PersistanceService persistanceService;

    public CommandExecutorService(PersistanceService persistanceService) {

        this.persistanceService = persistanceService;
    }

    public void executeCommand(LogEntry entry) {
        Object command = entry.getCommand();

        System.out.println("⚡ Aplicando comando: " + command.getClass().getSimpleName() +
                " no índice " + entry.getIndex());

        if (command instanceof UserSignUpCommand) {
            persistanceService.applyUserSignUpCommand((UserSignUpCommand) command);
        }
        // Adicionar outros comandos aqui:
        // else if (command instanceof SetDeckCommmand) {
        //     applySetDeckCommand((SetDeckCommmand) command);
        // }
        // ...
    }

    private void applyCard(){

    }

}