package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.DeckRepository;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.core.HazelcastInstance;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class DatabaseSyncService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final ClusterLeadershipService leadershipService;
    private final LeaderDiscoveryService discoveryService;
    private final HazelcastInstance hazelcast;

    private static final String SYNC_MAP = "sync-markers";
    private static final String SYNC_MARKER = "last-sync-timestamp";
    private final LeaderRedirectService leaderRedirectService;
    private final RestTemplate restTemplate;


    public DatabaseSyncService(UserRepository userRepository, DeckRepository deckRepository, ClusterLeadershipService leadershipService, LeaderDiscoveryService discoveryService,
                               @Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                               LeaderRedirectService leaderRedirectService,
                               RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.leadershipService = leadershipService;
        this.discoveryService = discoveryService;
        this.hazelcast = hazelcast;
        this.leaderRedirectService = leaderRedirectService;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initialSync() {
        // Usa uma nova thread para não bloquear o startup do Spring
        new Thread(() -> {
            try {
                // Dá um tempo para o cluster estabilizar e a eleição AP/CP ocorrer
                Thread.sleep(35000);
                System.out.println("🔍 Verificando necessidade de sincronização inicial...");

                if (isCurrentNodeLeader()) {
                    // O Líder apenas marca seu status e fica pronto para exportar
                    performLeaderSync();
                } else {
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
        System.out.println("👑 Este nó é líder - exportando dados de usuário.");
        return userRepository.findAll().stream()
                .map(UserExport::fromEntity)
                .toList();
    }

    public record UserExport(
            String id,
            String name,
            String nickname,
            String email,
            String password
    ) {
        // Construtor padrão necessário para RestTemplate.exchange()
        public UserExport {}

        public static UserExport fromEntity(User entity) {
            return new UserExport(
                    entity.getId().toString(),
                    entity.getName(),
                    entity.getNickname(),
                    entity.getEmail(),
                    entity.getPassword()
            );
        }

        public User toEntity() {
            User entity = new User();
            entity.setId(UUID.fromString(this.id));
            entity.setName(this.name);
            entity.setNickname(this.nickname);
            entity.setEmail(this.email);
            entity.setPassword(this.password);
            return entity;
        }
    }

    @Transactional
    public void performFollowerSync() {
        System.out.println("👥 Este nó é seguidor - iniciando sincronização com líder");

        discoveryService.getLeaderHttpAddress()
                .ifPresentOrElse(leaderUrl -> {
                    try {
                        String syncEndpoint = leaderUrl + "/api/sync/export/users";
                        System.out.println("🌐 Solicitando estado ao Líder em: " + syncEndpoint);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<Void> entity = new HttpEntity<>(headers);

                        UserExport[] usersToSyncArray = restTemplate.exchange(
                                syncEndpoint,
                                HttpMethod.GET,
                                entity,
                                UserExport[].class
                        ).getBody();

                        if (usersToSyncArray != null && usersToSyncArray.length > 0) {
                            List<User> entities = Arrays.stream(usersToSyncArray)
                                    .map(UserExport::toEntity)
                                    .toList();

                            userRepository.saveAll(entities);
                            System.out.println("✅ Sincronização de " + entities.size() + " usuários concluída com sucesso!");
                        } else {
                            System.out.println("✅ Sincronização concluída. Nenhum dado novo para importar.");
                        }

                    } catch (Exception e) {
                        System.err.println("❌ Erro fatal ao sincronizar com o líder: " + e.getMessage());
                    }
                }, () -> System.out.println("⚠️ Não foi possível encontrar o endereço HTTP do líder. Sincronização ignorada."));
    }

    // Tem que ter um post construct para sincronização inicial

    // tem que ter uma função para performar as persistencia de usuário cadastrados

    // Tem que ter uma função para performar as persistencia de Cartas abertas

    // Tem que ter uma função para performar

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

        newDeck.setId(UUID.randomUUID());
        newUser.setId(command.getPlayerId());
        newDeck.setUser(newUser);

        System.out.println("APLICA NO BANCO");

        userRepository.save(newUser);
        deckRepository.save(newDeck);
    }

    @Transactional
    public void applyOpenPackCommand(OpenPackCommand command){

    }

    public void propagateUserSignUpCommand(UserSignUpCommand command){
        System.out.println("CHEGOU NA PROPAGAÇÃO");
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .forEach(member -> {
                    String targetURL = String.format("http://%s:%d/api/sync/apply-commit/users",
                            member.getAddress().getHost(),
                            8080);
                    leaderRedirectService.sendCommandToNode(targetURL, command, HttpMethod.POST);
                });
    }

    public void progageteOpenPackCommand(OpenPackCommand command){

    }
}