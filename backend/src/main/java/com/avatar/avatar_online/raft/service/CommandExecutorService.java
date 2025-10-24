package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.TradeCardsCommand;
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
        } else if (command instanceof OpenPackCommand) {
            persistanceService.applyOpenPackCommand((OpenPackCommand) command);
        } else if (command instanceof SetDeckCommmand) {
            persistanceService.applySetDeckCommand((SetDeckCommmand) command);
        } else if (command instanceof TradeCardsCommand) {
            persistanceService.applyTradeCardCommand((TradeCardsCommand) command);
        }
    }

    private void applyCard() {

    }

}