package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.LeaderRedirectService;
import com.avatar.avatar_online.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ClusterLeadershipService leadershipService;
    private final LeaderRedirectService leaderRedirectService;


    public UserService(UserRepository userRepository,
                       ClusterLeadershipService leadershipService,
                       LeaderRedirectService leaderRedirectService) {
        this.userRepository = userRepository;
        this.leadershipService = leadershipService;
        this.leaderRedirectService = leaderRedirectService;
    }

    @Transactional
    public ResponseEntity<?> createUser(User user) {
        try {
            if (!leadershipService.isLeader()) {
                System.out.println("🚫 Este nó não é o líder. Redirecionando para o líder...");
                return leaderRedirectService.redirectToLeader("/api/users", user, HttpMethod.POST);
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
