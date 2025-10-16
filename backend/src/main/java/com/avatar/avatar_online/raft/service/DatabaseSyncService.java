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
    private final ClusterLeadershipService leadershipService;
    private final HazelcastInstance hazelcast;
    private final RedirectService redirectService;

    private static final long REPLICATION_TIMEOUT_SECONDS = 5;

    public DatabaseSyncService(UserRepository userRepository, DeckRepository deckRepository,
                               ClusterLeadershipService leadershipService,
                               LeaderDiscoveryService discoveryService, CardRepository cardRepository,
                               @Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                               RedirectService redirectService) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.leadershipService = leadershipService;
        this.cardRepository = cardRepository;
        this.hazelcast = hazelcast;
        this.redirectService = redirectService;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void initialSync() {
        // Usa uma nova thread para não bloquear o startup do Spring
        new Thread(() -> {
            try {
                // Dá um tempo para o cluster estabilizar e a eleição AP/CP ocorrer
                Thread.sleep(15000);

                System.out.println("🔍 Verificando necessidade de sincronização inicial...");

                if (!isCurrentNodeLeader()) {
                    performFollowerSync();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private boolean isCurrentNodeLeader(){
        return leadershipService.isLeader();
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

    @Transactional
    public void performFollowerSync() {
        System.out.println("👥 Este nó é seguidor - iniciando sincronização com líder");

        try {
            // --- SINCRONIZAÇÃO DE USUÁRIOS ---
            System.out.println("💾 Solicitando estado de usuários ao Líder");
            ResponseEntity<?> userResponse = redirectService.redirectToLeader("/api/sync/export/users", null, HttpMethod.GET);

            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                String userJsonBody = (String) userResponse.getBody();
                UserExport[] usersToSyncArray = new ObjectMapper().readValue(userJsonBody, UserExport[].class);

                if (usersToSyncArray.length > 0) {
                    List<User> entities = Arrays.stream(usersToSyncArray)
                            .map(UserExport::toEntity)
                            .toList();

                    userRepository.saveAll(entities);
                    System.out.println("✅ Sincronização de " + entities.size() + " usuários concluída com sucesso!");
                } else {
                    System.out.println("✅ Sincronização de usuários concluída. Nenhum dado novo para importar.");
                }
            }

            // --- SINCRONIZAÇÃO DE CARTAS ---
            System.out.println("💾 Solicitando estado de cartas ao Líder");
            ResponseEntity<?> cardResponse = redirectService.redirectToLeader("/api/sync/export/cards", null, HttpMethod.GET);

            if (cardResponse.getStatusCode().is2xxSuccessful() && cardResponse.getBody() != null) {
                String cardJsonBody = (String) cardResponse.getBody();
                CardExport[] cardsToSyncArray = new ObjectMapper().readValue(cardJsonBody, CardExport[].class);

                if (cardsToSyncArray.length > 0) {

                    Set<UUID> userIds = Arrays.stream(cardsToSyncArray)
                            .map(CardExport::userId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    List<User> referencedUsers = userRepository.findAllById(userIds);

                    Map<UUID, User> userMap = referencedUsers.stream()
                            .collect(Collectors.toMap(User::getId, user -> user));

                    List<Card> entities = Arrays.stream(cardsToSyncArray)
                            .map(export -> {
                                return export.toEntity(userMap);
                            })
                            .toList();

                    cardRepository.saveAll(entities);
                    System.out.println("✅ Sincronização de " + entities.size() + " cartas concluída com sucesso!");

                } else {
                    System.out.println("✅ Sincronização de cartas concluída. Nenhuma carta nova para importar.");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erro fatal ao sincronizar com o líder: " + e.getMessage());
        }

        // --- SINCRONIZAÇÃO DE DECKS ---
        System.out.println("💾 Solicitando estado de decks ao Líder");
        try {
            ResponseEntity<?> deckResponse = redirectService.redirectToLeader("/api/sync/export/decks", null, HttpMethod.GET);

            if (deckResponse.getStatusCode().is2xxSuccessful() && deckResponse.getBody() != null) {
                String deckJsonBody = (String) deckResponse.getBody();
                DeckExport[] decksToSyncArray = new ObjectMapper().readValue(deckJsonBody, DeckExport[].class);

                if (decksToSyncArray.length > 0) {

                    Set<UUID> userIds = Arrays.stream(decksToSyncArray)
                            .map(DeckExport::userId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    List<User> referencedUsers = userRepository.findAllById(userIds);
                    Map<UUID, User> userMap = referencedUsers.stream()
                            .collect(Collectors.toMap(User::getId, user -> user));

                    List<Deck> entities = Arrays.stream(decksToSyncArray)
                            .map(export -> export.toEntity(userMap))
                            .toList();

                    deckRepository.saveAll(entities);
                    System.out.println("✅ Sincronização de " + entities.size() + " decks concluída com sucesso!");
                } else {
                    System.out.println("✅ Sincronização de decks concluída. Nenhum deck novo para importar.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro fatal ao sincronizar decks: " + e.getMessage());
        }
    }


//    Métodos para sincronizar informações

    @Transactional
    public void applyDeckUpdateCommand(SetDeckCommmand command) {
        Optional<Deck> deckOptional = deckRepository.findByUser(command.getUserId());

        if (deckOptional.isEmpty()) {
            throw new RuntimeException("Deck de usuário com ID: " + command.getUserId() + " não encontrado!");
        }

        Deck deck = deckOptional.get();

        // é bom fazer uma verificação aqui dps para ver se todas as cartas são diferentes (Verificar o UUID)
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
    public void propagateUserSignUpCommand(UserSignUpCommand command){
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .forEach(member -> {
                    String targetURL = String.format("http://%s:%d/api/sync/apply-commit/users",
                            member.getAddress().getHost(),
                            8080);
                    redirectService.sendCommandToNode(targetURL, command, HttpMethod.POST);
                });
    }

    public boolean propagateLogEntry(LogEntry entry) { //Verificando essa lógica ainda

        Set<Member> allMembers = hazelcast.getCluster().getMembers();

        Set<Member> followers = allMembers.stream()
                .filter(member -> !member.localMember())
                .collect(Collectors.toSet());

        int clusterSize = allMembers.size();
        int majorityThreshold = (clusterSize / 2) + 1;

        if (clusterSize <= 1) return true; // Líder é a maioria

        System.out.println("📤 Iniciando replicação para " + followers.size() +
                " seguidores. Maioria: " + majorityThreshold);

        List<CompletableFuture<Boolean>> futures = followers.stream()
                .map(member -> CompletableFuture.supplyAsync(() -> {

                    String host = member.getAddress().getHost();
                    int port = 8080;

                    String targetUrl = String.format("http://%s:%d/api/sync/append-entries", host, port);

                    try {
                        System.out.println("   -> Enviando Log " + entry.getIndex() + " para: " + targetUrl);

                        redirectService.sendCommandToNode(targetUrl, entry, HttpMethod.POST);

                        return true;

                    } catch (Exception e) {
                        System.err.println("❌ Falha na replicação para nó " + member + ": " + e.getMessage());
                        return false;
                    }
                }))
                .toList();

        int successCount = 1;
        for (CompletableFuture<Boolean> future : futures) {
            try {
                if (future.get(REPLICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    successCount++;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.err.println("⏳ Timeout/Exceção na resposta da replicação.");
            }
        }

        boolean majorityReached = successCount >= majorityThreshold;

        if (majorityReached) {
            System.out.println("🎉 SUCESSO! Log persistido em " + successCount +
                    " nós (Maioria alcançada: " + majorityThreshold + ").");
        } else {
            System.out.println("🚨 FALHA NA MAIORIA! Apenas " + successCount +
                    " nós responderam (Necessário: " + majorityThreshold + ").");
        }

        return majorityReached;
    }

    @Async
    public void notifyFollowers(CommitNotificationRequest request){
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .forEach(member -> {
                    String targetURL = String.format("http://%s:%d/api/sync/commit-notification",
                            member.getAddress().getHost(),
                            8080);
                    redirectService.sendCommandToNode(targetURL, request, HttpMethod.POST);
                });
    }
}