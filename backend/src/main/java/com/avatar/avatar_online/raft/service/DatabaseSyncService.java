package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.Logs.OpenPackCommand;
import com.avatar.avatar_online.raft.Logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.UUID;

@Service
public class DatabaseSyncService {

    private final UserRepository userRepository;
    private final HazelcastInstance hazelcast;

    private static final String SYNC_MAP = "sync-markers";
    private static final String SYNC_MARKER = "last-sync-timestamp";
    private final LeaderRedirectService leaderRedirectService;
    private final RestTemplate restTemplate;


    public DatabaseSyncService(UserRepository userRepository,
                               @Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                               LeaderRedirectService leaderRedirectService,
                               RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.hazelcast = hazelcast;
        this.leaderRedirectService = leaderRedirectService;
        this.restTemplate = restTemplate;
    }

    // Tem que ter um post construct para sincronização inicial

    // tem que ter uma função para performar as persistencia de usuário cadastrados

    // Tem que ter uma função para performar as persistencia de Cartas abertas

    // Tem que ter uma função para performar

    @Transactional
    public void applyUserSignUpCommand(UserSignUpCommand command){
        User newUser = new User(
                command.getName(),
                command.getNickname(),
                command.getEmail(),
                command.getPassword()
        );

        newUser.setId(UUID.fromString(command.getPlayerId()));

        userRepository.save(newUser);
    }

    @Transactional
    public void applyOpenPackCommand(OpenPackCommand command){

    }

    public void propagateUserSignUpCommand(UserSignUpCommand command){
        hazelcast.getCluster().getMembers().stream()
                .filter(member -> !member.localMember())
                .forEach(member -> {
                    String targetURL = String.format("http://%s:%d/api/sync/apply-commit/users",
                            member.getAddress().getHost(),
                            8080);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Object> requestEntity = new HttpEntity<>(command, headers);

                    try {
                        // 🔑 Chamada bloqueante
                        restTemplate.exchange(targetURL, HttpMethod.POST, requestEntity, String.class);
                        System.out.println("✅ Propagado via RestTemplate para: " + targetURL);
                    } catch (Exception e) {
                        System.err.println("❌ Falha na propagação para " + targetURL + ": " + e.getMessage());
                    }
                });
    }

    public void progageteOpenPackCommand(OpenPackCommand command){

    }
}