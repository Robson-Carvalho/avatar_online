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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DatabaseSyncService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final HazelcastInstance hazelcast;

    private ScheduledExecutorService syncScheduler;
    private boolean syncActive = false;

    private static final String SYNC_MAP = "sync-markers";
    private static final String SYNC_MARKER = "last-sync-timestamp";

    public DatabaseSyncService(UserRepository userRepository,
                               JdbcTemplate jdbcTemplate,
                               @Qualifier("hazelcastInstance") HazelcastInstance hazelcast) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.hazelcast = hazelcast;
    }

    /**
     * Iniciado pelo líder para sincronização periódica
     */
    public void startLeaderSync() {
        if (syncActive) return;

        syncScheduler = Executors.newSingleThreadScheduledExecutor();
        syncActive = true;

        // Sincroniza a cada 60 segundos
        syncScheduler.scheduleAtFixedRate(() -> {
            if (isCurrentNodeLeader()) {
                performLeaderSync();
            }
        }, 0, 60, TimeUnit.SECONDS);

        System.out.println("🔄 Sincronização de líder iniciada");
    }

    public void stopLeaderSync() {
        if (syncScheduler != null) {
            syncScheduler.shutdown();
            try {
                if (!syncScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    syncScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                syncScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        syncActive = false;
        System.out.println("🛑 Sincronização de líder parada");
    }

    /**
     * Sincronização realizada pelo líder - EXPORTA dados
     */
    @Transactional(readOnly = true)
    public void performLeaderSync() {
        try {
            System.out.println("🔄 Líder exportando dados para sincronização...");

            // 1. Busca todos os usuários do banco local do líder
            List<UserEntity> allUsers = userRepository.findAll();

            // 2. Converte para formato simples para transferência
            List<UserExport> userExports = allUsers.stream()
                    .map(UserExport::fromEntity)
                    .toList();

            // 3. Armazena no mapa distribuído para seguidores pegarem
            IMap<String, Object> syncMap = hazelcast.getMap(SYNC_MAP);
            syncMap.put("user-export", userExports);
            syncMap.put(SYNC_MARKER, System.currentTimeMillis());

            System.out.println("✅ Líder exportou " + allUsers.size() + " usuários para sincronização");

        } catch (Exception e) {
            System.err.println("❌ Erro na exportação do líder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sincronização do seguidor - IMPORTA dados do líder
     */
    @Transactional
    public void performFollowerSync() {
        try {
            System.out.println("🔄 Seguidor importando dados do líder...");

            IMap<String, Object> syncMap = hazelcast.getMap(SYNC_MAP);

            // 1. Pega dados exportados pelo líder
            @SuppressWarnings("unchecked")
            List<UserExport> userExports = (List<UserExport>) syncMap.get("user-export");

            if (userExports == null || userExports.isEmpty()) {
                System.out.println("ℹ️  Nenhum dado para sincronizar");
                return;
            }

            // 2. Importa para o banco local
            int syncedCount = 0;
            for (UserExport userExport : userExports) {
                try {
                    // Usa SQL direto para upsert (insert ou update)
                    String sql = """
                        INSERT INTO app_user (id, name, nickname, email, password) 
                        VALUES (?, ?, ?, ?, ?) 
                        ON CONFLICT (id) DO UPDATE SET 
                            name = EXCLUDED.name,
                            nickname = EXCLUDED.nickname,
                            email = EXCLUDED.email,
                            password = EXCLUDED.password
                        """;

                    jdbcTemplate.update(sql,
                            userExport.id(),
                            userExport.name(),
                            userExport.nickname(),
                            userExport.email(),
                            userExport.password()
                    );

                    syncedCount++;
                } catch (Exception e) {
                    System.err.println("❌ Erro ao sincronizar usuário " + userExport.id() + ": " + e.getMessage());
                }
            }

            System.out.println("✅ Seguidor importou " + syncedCount + " usuários");

        } catch (Exception e) {
            System.err.println("❌ Erro na importação do seguidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sincroniza um novo nó que entrou no cluster
     */
    public void syncNewNode() {
        if (isCurrentNodeLeader()) {
            System.out.println("🔄 Líder sincronizando novo nó...");
            performLeaderSync();
        } else {
            System.out.println("🔄 Novo nó solicitando sincronização...");
            // Novo nó força sincronização com líder
            performFollowerSync();
        }
    }

    /**
     * Seguidores verificam se precisam sincronizar
     */
    public void checkSyncNeeded() {
        if (isCurrentNodeLeader()) return;

        IMap<String, Object> syncMap = hazelcast.getMap(SYNC_MAP);
        Long lastSync = (Long) syncMap.get(SYNC_MARKER);

        if (lastSync == null || isSyncNeeded(lastSync)) {
            System.out.println("🔍 Sincronização necessária detectada");
            performFollowerSync();
        }
    }

    /**
     * Verifica se este nó é o líder consultando diretamente o Hazelcast
     */
    private boolean isCurrentNodeLeader() {
        try {
            IMap<String, String> electionMap = hazelcast.getMap("leader-election");
            String currentLeader = electionMap.get("current-leader-node");
            String currentNodeId = hazelcast.getCluster().getLocalMember().getUuid().toString();

            return currentNodeId.equals(currentLeader);
        } catch (Exception e) {
            System.err.println("❌ Erro ao verificar liderança: " + e.getMessage());
            return false;
        }
    }

    private boolean isSyncNeeded(Long lastSync) {
        // Sincroniza se passou mais de 2 minutos desde última sinc
        return (System.currentTimeMillis() - lastSync) > 120000;
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
                    performLeaderSync();
                } else {
                    System.out.println("👥 Este nó é seguidor - sincronizando com líder");
                    checkSyncNeeded();
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
            UUID id,
            String name,
            String nickname,
            String email,
            String password
    ) {
        public static UserExport fromEntity(UserEntity entity) {
            return new UserExport(
                    entity.getId(),
                    entity.getName(),
                    entity.getNickname(),
                    entity.getEmail(),
                    entity.getPassword()
            );
        }
    }
}