package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.cp.lock.FencedLock;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CPCommitService {

    private final HazelcastInstance hazelcast;

    private static final String PACK_LOCK = "pack-lock";

    private static final String USER_LOCK = "user-lock";

    private static final String LAST_COMMAND_REF =  "last-db-command";

    private final DatabaseSyncService syncService;

    private final UserRepository userRepository;

    public CPCommitService(HazelcastInstance hazelcast, DatabaseSyncService syncService, UserRepository userRepository) {
        this.hazelcast = hazelcast;
        this.syncService = syncService;
        this.userRepository = userRepository;
    }

    public boolean tryCommitPackOpening(OpenPackCommand newCommand){
        FencedLock packLock = hazelcast.getCPSubsystem().getLock(PACK_LOCK);

        if(!packLock.tryLock()){
            System.out.println("⚠️ Não conseguiu o Lock. Outro nó está processando.");
            return false;
        }

        try{
            IAtomicReference<OpenPackCommand> commandRef = hazelcast.getCPSubsystem().getAtomicReference(LAST_COMMAND_REF);

            commandRef.set(newCommand);

            // 1. APLICAÇÃO NO BD LOCAL (DO LÍDER DA TRANSAÇÃO)
            // SE o set() foi bem-sucedido, aplique a instrução SQL no BD deste nó. syncService.applyCommandLocally(newCommand);

            // 2. PROPAGAÇÃO HTTP PARA OS SEGUIDORES
            // A chamada para o DatabaseSyncService (POST /apply-commit) vai aqui... syncService.propagateCommand(newCommand);

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erro ao comitar comando CP: " + e.getMessage());
            return false;
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
            return ResponseEntity.badRequest().body("{\"error\": \"Email já cadastrado\"}").hasBody();
        }

        if (newCommand.getNickname() != null && userRepository.existsByNickname(newCommand.getNickname())) {
            return ResponseEntity.badRequest().body("{\"error\": \"Nickname já cadastrado\"}").hasBody();
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
