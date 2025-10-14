package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.cp.lock.FencedLock;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CPCommitService {

    private final HazelcastInstance hazelcast;

    private static final String PACK_LOCK = "pack-lock";

    private static final String USER_LOCK = "user-lock";

    private static final String DECK_LOCK = "deck-lock";

    private static final String LAST_COMMAND_REF =  "last-db-command";

    private final DatabaseSyncService syncService;

    private final UserRepository userRepository;

    public CPCommitService(HazelcastInstance hazelcast, DatabaseSyncService syncService, UserRepository userRepository) {
        this.hazelcast = hazelcast;
        this.syncService = syncService;
        this.userRepository = userRepository;
    }

    public boolean tryCommitUpdateDeck(SetDeckCommmand newCommand) {
        System.out.println("chegou antes do lock");
        FencedLock decklock = hazelcast.getCPSubsystem().getLock(DECK_LOCK);

        System.out.println("ESSA PORRA CHEGOU NO TRYCOMMIT");

        if (!decklock.tryLock()) {
            System.out.println("⚠️ Não conseguiu o Lock. Outro nó está processando.");
            return false;
        }

        try{
           IAtomicReference<SetDeckCommmand> commandRef = hazelcast.getCPSubsystem().getAtomicReference(LAST_COMMAND_REF);

           commandRef.set(newCommand);

           syncService.applyDeckUpdateCommand(newCommand);

           syncService.propagateDeckUpdateCommand(newCommand);

           return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return false;
        } finally {
            decklock.unlock();
        }
    }

    public List<Card> tryCommitPackOpening(OpenPackCommand newCommand){
        System.out.println("chegou antes do lock");
        FencedLock packLock = hazelcast.getCPSubsystem().getLock(PACK_LOCK);

        System.out.println("ESSA PORRA CHEGOU NO TRYCOMMIT");

        if(!packLock.tryLock()){
            System.out.println("⚠️ Não conseguiu o Lock. Outro nó está processando.");
            return List.of();
        }

        try{
            IAtomicReference<OpenPackCommand> commandRef = hazelcast.getCPSubsystem().getAtomicReference(LAST_COMMAND_REF);

            commandRef.set(newCommand);

            // 1. APLICAÇÃO NO BD LOCAL (DO LÍDER DA TRANSAÇÃO)
            List<Card> cards = syncService.applyOpenPackCommand(newCommand);

            // 2. PROPAGAÇÃO HTTP PARA OS SEGUIDORES
            syncService.propagateOpenPackCommand(cards);

            return cards;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return List.of();
        } finally {
            // 3. Libera o Lock
            packLock.unlock();
        }
    }

    public boolean tryCommitUserSignUp(UserSignUpCommand newCommand){
        System.out.println("chegou antes do lock");

        FencedLock packLock = hazelcast.getCPSubsystem().getLock(USER_LOCK);

        System.out.println("ESSA PORRA CHEGOU NO TRYCOMMIT");

        if (userRepository.existsByEmail(newCommand.getEmail())) {
            return false;
        }

        if (newCommand.getNickname() != null && userRepository.existsByNickname(newCommand.getNickname())) {
            return false;
        }

        if(!packLock.tryLock()){
            System.out.println("⚠️ Não conseguiu o Lock. Outro nó está processando.");
            return false;
        }

        try{
            IAtomicReference<UserSignUpCommand> commandRef = hazelcast.getCPSubsystem().getAtomicReference(LAST_COMMAND_REF);

            commandRef.set(newCommand);

            // Aplica mudança no próprio DB
            syncService.applyUserSignUpCommand(newCommand);

            syncService.propagateUserSignUpCommand(newCommand);

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return false;
        } finally {
            // 3. Libera o Lock
            packLock.unlock();
        }
    }
}
