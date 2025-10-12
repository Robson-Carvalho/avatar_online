package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.model.UserExport;
import com.avatar.avatar_online.repository.CardRepository;
import com.avatar.avatar_online.repository.DeckRepository;
import com.avatar.avatar_online.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseSyncService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final ClusterLeadershipService leadershipService;
    private final HazelcastInstance hazelcast;
    private final RedirectService redirectService;

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

    @PostConstruct
    public void initialSync() {
        // Usa uma nova thread para não bloquear o startup do Spring
        new Thread(() -> {
            try {
                // Dá um tempo para o cluster estabilizar e a eleição AP/CP ocorrer
                Thread.sleep(15000);

                System.out.println("🔍 Verificando necessidade de sincronização inicial...");

                if (!isCurrentNodeLeader()) {
                    // O Seguidor busca o estado mais recente do Líder
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

    @Transactional
    public void performFollowerSync() {
        System.out.println("👥 Este nó é seguidor - iniciando sincronização com líder");

        try {
            System.out.println("💾 Solicitando estado ao Líder");

            ResponseEntity<?> response = redirectService.redirectToLeader("/api/sync/export/users", null, HttpMethod.GET);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String jsonBody = (String) response.getBody();
                UserExport[] usersToSyncArray = new ObjectMapper().readValue(jsonBody, UserExport[].class);

                if (usersToSyncArray.length > 0) {
                    List<User> entities = Arrays.stream(usersToSyncArray)
                            .map(UserExport::toEntity)
                            .toList();

                    userRepository.saveAll(entities);
                    System.out.println("✅ Sincronização de " + entities.size() + " usuários concluída com sucesso!");
                } else {
                    System.out.println("✅ Sincronização concluída. Nenhum dado novo para importar.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro fatal ao sincronizar com o líder: " + e.getMessage());
        }
    }


//    Métodos para sincronizar informações

    @Transactional
    public List<Card> applyOpenPackCommand(OpenPackCommand command) {
        int packSize = 5;
        UUID userId = command.getPlayerId();

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

        System.out.println("APLICA NO BANCO");

        User user = userOptional.get();
        selectedCards.forEach(card -> card.setUser(user));
        cardRepository.saveAll(selectedCards);

        return selectedCards;
    }

    public void propagateOpenPackCommand(List<Card> cards){
        System.out.println("PROPAGA");
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
        System.out.println("APLICA NO BANCO: Follower");
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

        System.out.println("APLICA NO BANCO");

        userRepository.save(newUser);
        deckRepository.save(newDeck);
    }


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
}