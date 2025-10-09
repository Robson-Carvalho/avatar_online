package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.UserEntity;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DatabaseSyncService {

    private ScheduledExecutorService syncScheduler;
    private boolean syncActive = false;

    private static final String SYNC_MAP = "sync-markers";
    private static final String SYNC_MARKER = "last-sync-timestamp";

    public DatabaseSyncService(UserRepository userRepository,
                               JdbcTemplate jdbcTemplate,
                               @Qualifier("hazelcastInstance") HazelcastInstance hazelcast) {
    }

    /**
     * Iniciado pelo líder para sincronização periódica
     */
    public void startLeaderSync() {
    }

    public void stopLeaderSync() {

    }

    /**
     * Sincronização realizada pelo líder - EXPORTA dados
     */
    @Transactional(readOnly = true)
    public void performLeaderSync() {

    }

    /**
     * Sincronização do seguidor - IMPORTA dados do líder
     */
    @Transactional
    public void performFollowerSync() {

    }

    /**
     * Sincroniza um novo nó que entrou no cluster
     */
    public void syncNewNode() {

    }

    /**
     * Seguidores verificam se precisam sincronizar
     */
    public void checkSyncNeeded() {

    }

    /**
     * Verifica se este nó é o líder consultando diretamente o Hazelcast
     */
    private boolean isCurrentNodeLeader() {
        return true;
    }

    private boolean isSyncNeeded(Long lastSync) {
        return true;
    }

    /**
     * Sincronização inicial quando nó entra no cluster
     */
    @PostConstruct
    public void initialSync() {
        // Aguarda cluster estabilizar
        new Thread(() -> {
            try {
                Thread.sleep(15000); // 15 segundos
                System.out.println("🔍 Verificando necessidade de sincronização inicial...");

                if (isCurrentNodeLeader()) {
                    System.out.println("👑 Este nó é líder - exportando dados iniciais");
                } else {
                    System.out.println("👥 Este nó é seguidor - sincronizando com líder");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Força sincronização manual (útil para testes)
     */
    public void forceSync() {
        if (isCurrentNodeLeader()) {
            performLeaderSync();
        } else {
            performFollowerSync();
        }
    }

    /**
     * Classe simples para transferência de dados
     */
    public record UserExport(
            String id,
            String name,
            String nickname,
            String email,
            String password
    ) {
        public static UserExport fromEntity(UserEntity entity) {
            return new UserExport(
                    entity.getId().toString(),
                    entity.getName(),
                    entity.getNickname(),
                    entity.getEmail(),
                    entity.getPassword()
            );
        }
    }
}