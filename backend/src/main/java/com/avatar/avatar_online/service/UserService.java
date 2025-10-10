package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.Logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.service.CPCommitService;
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
    private final CPCommitService cPCommitService;


    public UserService(UserRepository userRepository,
                       ClusterLeadershipService leadershipService,
                       LeaderRedirectService leaderRedirectService, CPCommitService cPCommitService) {
        this.userRepository = userRepository;
        this.leadershipService = leadershipService;
        this.leaderRedirectService = leaderRedirectService;
        this.cPCommitService = cPCommitService;
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

            UserSignUpCommand command = new UserSignUpCommand(UUID.randomUUID(), "SIGN_USER", user.getId(), user.getName(),
                    user.getEmail(), user.getNickname(), user.getPassword());

            boolean response = cPCommitService.tryCommitUserSignUp(command);

            if(!response){
                return ResponseEntity.badRequest().body("");
            }

            System.out.println("✅ Usuário criado pelo líder: ");
            return ResponseEntity.ok().body("");
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
