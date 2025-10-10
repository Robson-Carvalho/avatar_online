package com.avatar.avatar_online.raft.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    private final String nodeId;

    @Value("${app.cluster.port:5701}")
    private int clusterPort;

    @Autowired
    public HazelcastConfig(NodeIDConfig nodeIDConfig) {
        this.nodeId = nodeIDConfig.getNodeId();
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        System.out.println("🚀 Iniciando Hazelcast - Nó: " + nodeId + ", Porta: " + clusterPort);

        Config config = new Config();

        config.setClusterName("avatar-cluster");
        config.setInstanceName("avatar-node-" + nodeId);
        config.setProperty("hazelcast.tcp.join.timeout.seconds", "1");

        config.setProperty("hazelcast.shutdownhook.enabled", "false");
        config.setProperty("hazelcast.phone.home.enabled", "false");

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(clusterPort)
                .setPortAutoIncrement(false);


        networkConfig.getJoin().getMulticastConfig().setEnabled(true);

        MapConfig leaderMapConfig = new MapConfig();
        leaderMapConfig.setName("leader-registry")
                .setTimeToLiveSeconds(30);

        MapConfig electionMapConfig = new MapConfig();
        electionMapConfig.setName("leader-election")
                .setTimeToLiveSeconds(30);

        config.getCPSubsystemConfig()
                .setCPMemberCount(3);

        config.addMapConfig(leaderMapConfig);
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