package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.model.*;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class DatabaseSyncService {

    private final HazelcastInstance hazelcast;
    private final RedirectService redirectService;
    private final LogConsensusService logConsensusService; // Pode dar dependencia circular
    private final LogStore logStore; // Pode dar dependencia circular
    private final LeaderRegistryService leaderRegistryService;

    private static final long REPLICATION_TIMEOUT_SECONDS = 5;

    public DatabaseSyncService(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                               RedirectService redirectService, LogConsensusService logConsensusService,
                               LogStore logStore, LeaderRegistryService leaderRegistryService) {
        this.hazelcast = hazelcast;
        this.redirectService = redirectService;
        this.logConsensusService = logConsensusService;
        this.logStore = logStore;
        this.leaderRegistryService = leaderRegistryService;
    }

//    Métodos para sincronizar informações

    @Async
    public void sendHeartbeat() {
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .forEach(member -> {
                    sendAppendEntries(member, false);
                });
    }

    public boolean propagateLogEntry() {

        Set<Member> allMembers = hazelcast.getCluster().getMembers();
        Set<Member> followers = allMembers.stream().filter(member -> !member.localMember()).collect(Collectors.toSet());

        List<CompletableFuture<Boolean>> futures = followers.stream()
                .map(member -> CompletableFuture.supplyAsync(() -> {
                    System.out.println("\uD83D\uDC95 Heartbeat enviado para propagação de log.");
                    return sendAppendEntries(member, true);
                }))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allOf.get(REPLICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("⏳ Timeout/Exceção na resposta da replicação. Processando as respostas disponíveis.");
        }

        int successCount = 1;
        for (CompletableFuture<Boolean> future : futures) {
            if (future.isDone() && !future.isCompletedExceptionally()) {
                if (future.join()) {
                    successCount++;
                }
            }
        }

        int clusterSize = allMembers.size();
        int majorityThreshold = (clusterSize / 2) + 1;
        boolean majorityReached = successCount >= majorityThreshold;

        if (majorityReached) {
            System.out.println("🎉 SUCESSO! Log persistido em " + successCount + " nós (Maioria alcançada: " + majorityThreshold + ").");
        } else {
            System.out.println("🚨 FALHA NA MAIORIA! Apenas " + successCount + " nós responderam (Necessário: " + majorityThreshold + ").");
        }

        return majorityReached;
    }

    /**
     * Método privado central para enviar a RPC AppendEntries para um único seguidor.
     * Lida com o cálculo de nextIndex/matchIndex e o reparo de log.
     * @param member O membro seguidor.
     * @param mustUpdateIndexes Se deve processar a resposta para atualizar matchIndex (true para replicação, false para heartbeat).
     * @return true se a RPC foi bem-sucedida (o log foi anexado ou o heartbeat foi aceito).
     */
    private boolean sendAppendEntries(Member member, boolean mustUpdateIndexes) {
        UUID followerId = member.getUuid();
        String targetUrl = String.format("http://%s:%d/api/sync/append-entries", member.getAddress().getHost(), 8080);

        long nextIndexForFollower = logConsensusService.getNextIndex(followerId);

        List<LogEntry> entriesToSend = logStore.getEntriesFrom(nextIndexForFollower);

        long prevLogIndex = nextIndexForFollower - 1;
        long prevLogTerm = logStore.getTermOfIndex(prevLogIndex);

        AppendEntriesRequest request = new AppendEntriesRequest(
                leaderRegistryService.getCurrentTerm(),
                logStore.getLastCommitIndex(),
                prevLogIndex,
                prevLogTerm,
                entriesToSend
        );

        try {
            if (!entriesToSend.isEmpty()) {
                //System.out.println("   -> Enviando logs a partir do índice " + nextIndexForFollower + " para: " + member.getAddress().getHost());
            } else {
                //System.out.println("   -> Enviando Heartbeat para: " + member.getAddress().getHost());
            }

            ResponseEntity<AppendEntriesResponse> responseEntity = redirectService.sendCommandToNode(
                    targetUrl, request, HttpMethod.POST, AppendEntriesResponse.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new RuntimeException("Resposta inválida ou erro HTTP.");
            }

            AppendEntriesResponse response = responseEntity.getBody();

            if (response.isSuccess()) {

                if (mustUpdateIndexes) {
                    int entriesSentCount = entriesToSend.size();
                    if (entriesSentCount > 0) {
                        long newMatchIndex = prevLogIndex + entriesSentCount;
                        logConsensusService.updateIndexesOnSuccess(followerId, newMatchIndex);
                        System.out.println("✅ Log sync em " + followerId + ". Novo MatchIndex: " + newMatchIndex);
                    }
                }

                return true;

            } else if (response.isLogMismatch()) {

                logConsensusService.decrementNextIndex(followerId);
                long newNextIndex = logConsensusService.getNextIndex(followerId);

                System.err.println("❌ Log Mismatch com " + followerId + ". Recuando NextIndex para: " + newNextIndex);
                return false;

            } else {
                //System.out.println("Falha genérica ou termo obsoleto no nó: " + followerId);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Falha na replicação/heartbeat para nó " + member + ": " + e.getMessage());
            return false;
        }
    }
}