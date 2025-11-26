package com.avatar.avatar_online.service;

import com.avatar.avatar_online.DTOs.Create_WalletDTO;
import com.avatar.avatar_online.DTOs.UserDTO;
import com.avatar.avatar_online.Truffle_Comunication.TruffleApiUser;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.service.CPCommitService;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.RedirectService;
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
    private final RedirectService redirectService;
    private final CPCommitService cPCommitService;
    private final TruffleApiUser truffleApiUser;


    public UserService(UserRepository userRepository,
                       ClusterLeadershipService leadershipService,
                       RedirectService leaderRedirectService, CPCommitService cPCommitService,
                       TruffleApiUser truffleApiUser) {
        this.userRepository = userRepository;
        this.leadershipService = leadershipService;
        this.redirectService = leaderRedirectService;
        this.cPCommitService = cPCommitService;
        this.truffleApiUser = truffleApiUser;
    }

    @Transactional
    public ResponseEntity<?> createUser(UserDTO user) {
        System.out.println("⚠️ createUser chamado no nó: " + leadershipService.isLeader() + "  |  " + System.currentTimeMillis());
        try {
            if (!leadershipService.isLeader()) {
                System.out.println("🚫 Este nó não é o líder. Redirecionando para o líder...");
                return redirectService.redirectToLeader("/api/users", user, HttpMethod.POST);
            }

            System.out.println("⚠️ createUser chamado no nó: " + leadershipService.isLeader() + "  |  " + System.currentTimeMillis());

            ResponseEntity<Create_WalletDTO> responseEntity = truffleApiUser.createWallet();

            Create_WalletDTO walletDTO = responseEntity.getBody();

            if (walletDTO == null ||
                    walletDTO.getData() == null ||
                    walletDTO.getData().getAddress() == null ||
                    walletDTO.getData().getPrivate_key() == null) {

                return ResponseEntity.internalServerError().body("Falha ao criar carteira na blockchain.");
            }

            UserSignUpCommand command = new UserSignUpCommand(UUID.randomUUID(), "SIGN_USER",UUID.randomUUID(), UUID.randomUUID(), user.getName(),
                    user.getEmail(), user.getNickname(), user.getPassword(), walletDTO.getData().getPrivate_key(), walletDTO.getData().getAddress());

            boolean response = cPCommitService.tryCommitUserSignUp(command);

            if(!response){
                return ResponseEntity.badRequest().body("Erro interno: Falha ao processar requisição. " +
                        "Email ou senha podem já estar sendo utilizados.");
            }

            User user1 = new User(command.getPlayerId(), user.getName(), user.getNickname(),
                    user.getEmail(), user.getPassword(), walletDTO.getData().getPrivate_key(), walletDTO.getData().getAddress());

            return ResponseEntity.ok().body(user1);
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

    public ResponseEntity<String> getHistory(){
        ResponseEntity<String> truffleResponse = truffleApiUser.getHistory();

        String body = truffleResponse.getBody();

        if (body == null) {
            return ResponseEntity.internalServerError().body("Erro ao obter histórico");
        }

        return ResponseEntity.ok(body);
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
