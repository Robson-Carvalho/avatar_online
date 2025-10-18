package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.model.*;
import com.avatar.avatar_online.repository.CardRepository;
import com.avatar.avatar_online.repository.DeckRepository;
import com.avatar.avatar_online.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class DatabaseSyncService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final HazelcastInstance hazelcast;
    private final RedirectService redirectService;
    private final LogConsensusService logConsensusService; // Pode dar dependencia circular
    private final LogStore logStore; // Pode dar dependencia circular
    private final LeaderRegistryService leaderRegistryService;

    private static final long REPLICATION_TIMEOUT_SECONDS = 5;

    public DatabaseSyncService(UserRepository userRepository, DeckRepository deckRepository, CardRepository cardRepository,
                               @Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                               RedirectService redirectService, LogConsensusService logConsensusService,
                               LogStore logStore, LeaderRegistryService leaderRegistryService) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.hazelcast = hazelcast;
        this.redirectService = redirectService;
        this.logConsensusService = logConsensusService;
        this.logStore = logStore;
        this.leaderRegistryService = leaderRegistryService;
    }

    @Transactional(readOnly = true)
    public List<UserExport> performLeaderSync() {
        System.out.println("💾 Líder - exportando dados de usuário.");

        return userRepository.findAll().stream()
                .map(UserExport::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardExport> performLeaderCardSync() {
        System.out.println("💾 Líder - exportando dados de cartas.");

        // Retorna todas as cartas, mapeadas para CardExport
        return cardRepository.findAll().stream()
                .map(CardExport::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeckExport> performLeaderDeckSync() {
        System.out.println("💾 Líder - exportando dados de decks.");

        return deckRepository.findAll().stream()
                .map(DeckExport::fromEntity)
                .toList();
    }


//    Métodos para sincronizar informações

    @Transactional
    public void applyDeckUpdateCommand(SetDeckCommmand command) {
        Optional<Deck> deckOptional = deckRepository.findByUser(command.getUserId());

        if (deckOptional.isEmpty()) {
            throw new RuntimeException("Deck de usuário com ID: " + command.getUserId() + " não encontrado!");
        }

        Deck deck = deckOptional.get();

        System.out.println(command.getCard1Id());
        deck.setCard1(command.getCard1Id());
        deck.setCard2(command.getCard2Id());
        deck.setCard3(command.getCard3Id());
        deck.setCard4(command.getCard4Id());
        deck.setCard5(command.getCard5Id());

        deckRepository.save(deck);
    }

    @Async
    public void propagateDeckUpdateCommand(SetDeckCommmand command) {
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .forEach(member -> {
                    String targetURL = String.format("http://%s:%d/api/sync/apply-commit/deck",
                            member.getAddress().getHost(),
                            8080);
                    redirectService.sendCommandToNode(targetURL, command, HttpMethod.POST);
                });
    }

    @Transactional
    public List<Card> applyOpenPackCommand(OpenPackCommand command) {
        int packSize = 5;
        UUID userId = command.getPlayerId();

        // Em vez de gerar isso aqui, a gente gera antes quais cartas serão retiradas e dps envia para todo mundo

        List<Card> availableCards = cardRepository.findAll()
                .stream()
                .filter(card -> card.getUser() == null)
                .collect(Collectors.toList());

        if (availableCards.size() < packSize) {
            throw new RuntimeException("Não há cartas suficientes disponíveis!");
        }

        Collections.shuffle(availableCards);
        List<Card> selectedCards = availableCards.subList(0, packSize);

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado com ID: " + userId);
        }

        User user = userOptional.get();
        selectedCards.forEach(card -> card.setUser(user));
        cardRepository.saveAll(selectedCards);

        return selectedCards;
    }

    @Async
    public void propagateOpenPackCommand(List<Card> cards){
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .forEach(member -> {
                    String targetURL = String.format("http://%s:%d/api/sync/apply-commit/pack",
                            member.getAddress().getHost(),
                            8080);
                    redirectService.sendCommandToNode(targetURL, cards, HttpMethod.POST);
                });
    }

    @Transactional
    public void applyPackCommit(List<Card> cards) {
        cards.forEach(card -> {
            cardRepository.findById(card.getId()).ifPresent(dbCard -> {
                dbCard.setUser(card.getUser());
                cardRepository.save(dbCard);
            });
        });
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

        userRepository.save(newUser);
        deckRepository.save(newDeck);
    }

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

        int successCount = 1; // O líder conta a si mesmo
        for (CompletableFuture<Boolean> future : futures) {
            try {
                if (future.get(REPLICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    successCount++;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.err.println("⏳ Timeout/Exceção na resposta da replicação.");
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
                System.out.println("   -> Enviando logs a partir do índice " + nextIndexForFollower + " para: " + member.getAddress().getHost());
            } else {
                System.out.println("   -> Enviando Heartbeat para: " + member.getAddress().getHost());
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
                System.out.println("Falha genérica ou termo obsoleto no nó: " + followerId);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Falha na replicação/heartbeat para nó " + member + ": " + e.getMessage());
            return false;
        }
    }
}