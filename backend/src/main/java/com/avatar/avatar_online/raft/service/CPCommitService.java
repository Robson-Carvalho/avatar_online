package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.model.LogEntry;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.cp.lock.FencedLock;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final LogStore logStore;

    private final LeaderRegistryService leaderRegistryService;

    public CPCommitService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast, DatabaseSyncService syncService,
                           UserRepository userRepository, LogStore logStore,
                           LeaderRegistryService leaderRegistryService) {
        this.hazelcast = hazelcast;
        this.syncService = syncService;
        this.userRepository = userRepository;
        this.logStore = logStore;
        this.leaderRegistryService = leaderRegistryService;
    }

    public boolean tryCommitUpdateDeck(SetDeckCommmand newCommand) {
        if(hazelcast.getCluster().getMembers().size() < 2) {
            System.out.println("LOGS: TAMANHO DE CLUSTER INSUFICIENTE PARA REALIZAR OPERAÇÕES CRÍTICAS.");
            return false;
        }

        try{
           syncService.applyDeckUpdateCommand(newCommand);

           syncService.propagateDeckUpdateCommand(newCommand);

           return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP aaaa: " + e.getMessage());
            return false;
        }
    }

    public List<Card> tryCommitPackOpening(OpenPackCommand newCommand){
        if(hazelcast.getCluster().getMembers().size() < 2) {
            System.out.println("LOGS: TAMANHO DE CLUSTER INSUFICIENTE PARA REALIZAR OPERAÇÕES CRÍTICAS.");
            return List.of();
        }

        try{
            // 1. APLICAÇÃO NO BD LOCAL (DO LÍDER DA TRANSAÇÃO)
            List<Card> cards = syncService.applyOpenPackCommand(newCommand);

            // 2. PROPAGAÇÃO HTTP PARA OS SEGUIDORES
            syncService.propagateOpenPackCommand(cards);

            return cards;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return List.of();
        }
    }

    public boolean tryCommitUserSignUp(UserSignUpCommand newCommand){ //Verificando essa lógica ainda
        if(hazelcast.getCluster().getMembers().size() < 2) {
            System.out.println("LOGS: TAMANHO DE CLUSTER INSUFICIENTE PARA REALIZAR OPERAÇÕES CRÍTICAS.");
            return false;
        }

        if (userRepository.existsByEmail(newCommand.getEmail())) {
            return false;
        }

        if (newCommand.getNickname() != null && userRepository.existsByNickname(newCommand.getNickname())) {
            return false;
        }
        try{
            long newIndex = logStore.getLastIndex() + 1;
            long currentTerm = leaderRegistryService.getCurrentTerm();
            LogEntry newLogEntry = new LogEntry(currentTerm, newIndex, newCommand, false);

            logStore.append(newLogEntry);

            boolean majorityReplied = syncService.propagateLogEntry(newLogEntry);

            if(!majorityReplied){
                System.out.println("Falha na replicação para a maioria. Comando não commmitado.");
                return false;
            }

            logStore.markCommitted(newIndex);

            // FAZER UMA CHAMADA A UMA FUNÇÃO DE PROPAGAÇÃO INFORMANDO QUE O COMMIT DEVE SER APLICADNO
            // MAIS UM ENDPOIN

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return false;
        }
    }
}
