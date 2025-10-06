package com.avatar.avatar_online.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class HazelcastConfig {

    @Value("${app.node.id:node-1}")
    private String nodeId;

    @Value("${app.cluster.port:5701}")
    private int clusterPort;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        System.out.println("🚀 Iniciando Hazelcast - Nó: " + nodeId + ", Porta: " + clusterPort);

        Config config = new Config();

        // Configurações básicas
        config.setClusterName("avatar-cluster");
        config.setInstanceName("avatar-node-" + nodeId);

        // 🔥 DESABILITA SHUTDOWN HOOK AUTOMÁTICO
        config.setProperty("hazelcast.shutdownhook.enabled", "false");
        config.setProperty("hazelcast.phone.home.enabled", "false");

        // Configuração de rede SIMPLIFICADA
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(clusterPort)
                .setPortAutoIncrement(false); // Porta fixa

        // Apenas localhost para desenvolvimento
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig()
                .setEnabled(true)
                .setMembers(Arrays.asList("127.0.0.1"));

        // Configuração de Maps
        MapConfig leaderMapConfig = new MapConfig();
        leaderMapConfig.setName("leader-registry")
                .setTimeToLiveSeconds(30);

        MapConfig syncMapConfig = new MapConfig();
        syncMapConfig.setName("sync-markers")
                .setTimeToLiveSeconds(3600);

        MapConfig electionMapConfig = new MapConfig();
        electionMapConfig.setName("leader-election")
                .setTimeToLiveSeconds(30);

        config.addMapConfig(leaderMapConfig);
        config.addMapConfig(syncMapConfig);
        config.addMapConfig(electionMapConfig);

        try {
            HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
            System.out.println("✅ Hazelcast iniciado com sucesso!");
            return instance;
        } catch (Exception e) {
            System.err.println("❌ Erro ao iniciar Hazelcast: " + e.getMessage());
            throw e;
        }
    }
}