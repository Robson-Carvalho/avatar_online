package com.avatar.service;

import com.avatar.DTOs.UserDTO;
import com.avatar.models.User;
import com.avatar.raft.logs.UserSignUpCommand;
import com.avatar.raft.service.CPCommitService;
import com.avatar.raft.service.ClusterLeadershipService;
import com.avatar.raft.service.RedirectService;
import com.avatar.repository.UserRepository;
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
    private final RedirectService redirectService;
    private final CPCommitService cPCommitService;


    public UserService(UserRepository userRepository,
                       ClusterLeadershipService leadershipService,
                       RedirectService leaderRedirectService, CPCommitService cPCommitService) {
        this.userRepository = userRepository;
        this.leadershipService = leadershipService;
        this.redirectService = leaderRedirectService;
        this.cPCommitService = cPCommitService;
    }

    @Transactional
    public ResponseEntity<?> createUser(UserDTO user) {
        try {
            if (!leadershipService.isLeader()) {
                System.out.println("🚫 Este nó não é o líder. Redirecionando para o líder...");
                return redirectService.redirectToLeader("/api/users", user, HttpMethod.POST);
            }

            UserSignUpCommand command = new UserSignUpCommand(UUID.randomUUID(), "SIGN_USER",UUID.randomUUID(), UUID.randomUUID(), user.getName(),
                    user.getEmail(), user.getNickname(), user.getPassword());

            boolean response = cPCommitService.tryCommitUserSignUp(command);

            if(!response){
                return ResponseEntity.badRequest().body("Erro interno: Falha ao processar requisição. " +
                        "Email ou senha podem já estar sendo utilizados.");
            }

            return ResponseEntity.ok().body(command.getPlayerId());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<?> createUserFake(UserDTO user) {
        try {
           boolean response = true;

            if(!response){
                return ResponseEntity.badRequest().body("Erro interno: Falha ao processar requisição. " +
                        "Email ou senha podem já estar sendo utilizados.");
            }

            User userFake = new User(UUID.randomUUID(), user.getName(), user.getEmail(), user.getNickname(), user.getPassword());

            return ResponseEntity.ok().body(userFake);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }

    public Optional<User> login(String nickname, String password) {
        Optional<User> user = this.findByNickname(nickname);

        if(user.isPresent()){
            if(user.get().getPassword().equals(password)){
                return user;
            }

            return Optional.empty();
        }

        return Optional.empty();
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
