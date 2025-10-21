package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.model.LogEntry;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

@Service
public class CPCommitService {

    private final HazelcastInstance hazelcast;

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
             long newIndex = logStore.getLastIndex() + 1;
             long currentTerm = leaderRegistryService.getCurrentTerm();
             LogEntry newLogEntry = new LogEntry(currentTerm, newIndex, newCommand, false);

             logStore.append(newLogEntry);

             boolean majorityReplied = syncService.propagateLogEntry();

             if(!majorityReplied){
                 System.out.println("Falha na replicação para a maioria. Comando não commmitado.");
                 return false;
             }

             logStore.tryAdvanceCommitIndex(currentTerm, logStore.getLastIndex());

             return true;
        }catch (Exception e){
            System.out.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return false;
        }
    }

    public boolean tryCommitPackOpening(OpenPackCommand newCommand){
        if(hazelcast.getCluster().getMembers().size() < 2) {
            System.out.println("LOGS: TAMANHO DE CLUSTER INSUFICIENTE PARA REALIZAR OPERAÇÕES CRÍTICAS.");
            return false;
        }

        try{
            long newIndex = logStore.getLastIndex() + 1;  // Considerar refatorar isso aqui para deixar mais elegante
            long currentTerm = leaderRegistryService.getCurrentTerm();
            LogEntry newLogEntry = new LogEntry(currentTerm, newIndex, newCommand, false);

            logStore.append(newLogEntry);

            boolean majorityReplied = syncService.propagateLogEntry();

            if(!majorityReplied){
                System.out.println("Falha na replicação para a maioria. Comando não commmitado.");
                return false;
            }

            logStore.tryAdvanceCommitIndex(currentTerm,  logStore.getLastIndex());

            return true;
        } catch (Exception e) {
            System.out.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return false;
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

            boolean majorityReplied = syncService.propagateLogEntry();

            if(!majorityReplied){
                System.out.println("Falha na replicação para a maioria. Comando não commmitado.");
                return false;
            }

            logStore.tryAdvanceCommitIndex(currentTerm,  logStore.getLastIndex());

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return false;
        }
    }
}
