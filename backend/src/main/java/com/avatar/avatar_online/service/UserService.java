package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.UserEntity;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.DatabaseSyncService;
import com.avatar.avatar_online.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ClusterLeadershipService leadershipService;
    private final DatabaseSyncService databaseSyncService;

    public UserService(UserRepository userRepository,
                       ClusterLeadershipService leadershipService,
                       DatabaseSyncService databaseSyncService) {
        this.userRepository = userRepository;
        this.leadershipService = leadershipService;
        this.databaseSyncService = databaseSyncService;
    }

    /**
     * Cria usuário apenas se este nó for o líder
     */
    @Transactional
    public UserEntity createUser(UserEntity user) {
        if (!leadershipService.isLeader()) {
            throw new IllegalStateException(
                    "Apenas o nó líder pode criar usuários. " +
                            "Este nó não é o líder."
            );
        }

        // Verifica se email ou nickname já existem
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (user.getNickname() != null && userRepository.existsByNickname(user.getNickname())) {
            throw new IllegalArgumentException("Nickname já cadastrado");
        }

        // Salva no banco do líder
        UserEntity savedUser = userRepository.save(user);

        System.out.println("✅ Usuário criado pelo líder: " + savedUser.getEmail());

        // 🔥 FORÇA SINCRONIZAÇÃO IMEDIATA
        databaseSyncService.performLeaderSync();

        return savedUser;
    }

    /**
     * Busca por ID - consulta LOCAL no banco deste nó
     */
    public Optional<UserEntity> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Busca por email - consulta LOCAL
     */
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Busca por nickname - consulta LOCAL
     */
    public Optional<UserEntity> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    /**
     * Verifica se email existe - consulta LOCAL
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Verifica se nickname existe - consulta LOCAL
     */
    public boolean nicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * Lista todos os usuários - consulta LOCAL
     */
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    /**
     * Conta usuários - consulta LOCAL
     */
    public long count() {
        return userRepository.count();
    }
}