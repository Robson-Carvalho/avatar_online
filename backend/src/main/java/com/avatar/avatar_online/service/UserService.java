package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.DatabaseSyncService;
import com.avatar.avatar_online.raft.service.LeaderDiscoveryService;
import com.avatar.avatar_online.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ClusterLeadershipService leadershipService;
    private final DatabaseSyncService databaseSyncService;
    private final LeaderDiscoveryService leaderDiscoveryService;
    private final RestTemplate restTemplate;

    public UserService(UserRepository userRepository,
                       ClusterLeadershipService leadershipService,
                       DatabaseSyncService databaseSyncService,
                       LeaderDiscoveryService leaderDiscoveryService,
                       RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.leadershipService = leadershipService;
        this.databaseSyncService = databaseSyncService;
        this.leaderDiscoveryService = leaderDiscoveryService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public ResponseEntity<?> createUser(User user) {
        try {
            if (!leadershipService.isLeader()) {
                System.out.println("🚫 Este nó não é o líder. Redirecionando para o líder...");
                return redirectToLeader("/api/users", user, HttpMethod.POST);
            }

            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("{\"error\": \"Email já cadastrado\"}");
            }

            if (user.getNickname() != null && userRepository.existsByNickname(user.getNickname())) {
                return ResponseEntity.badRequest().body("{\"error\": \"Nickname já cadastrado\"}");
            }

            User savedUser = userRepository.save(user);
            System.out.println("✅ Usuário criado pelo líder: " + savedUser.getEmail());

            //databaseSyncService.performLeaderSync();
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }

    private ResponseEntity<?> redirectToLeader(String path, Object body, HttpMethod method) {
        try {
            Optional<String> leaderAddress = leaderDiscoveryService.getLeaderHttpAddress();

            if (leaderAddress.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("{\"error\": \"Líder não encontrado no cluster. Tente novamente.\"}");
            }

            String leaderUrl = leaderAddress.get() + path;
            System.out.println("🔄 Redirecionando requisição para o líder: " + leaderUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    leaderUrl, method, requestEntity, String.class
            );

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\": \"Falha ao redirecionar para o líder: " + e.getMessage() + "\"}");
        }
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean nicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public long count() {
        return userRepository.count();
    }
}
