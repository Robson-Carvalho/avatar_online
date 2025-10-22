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

    @Value("${HAZELCAST_PUBLIC_ADDRESS:}")
    private String publicAddress;

    @Value("${HAZELCAST_MEMBER_LIST:}")
    private String memberList;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        System.out.println("🚀 Iniciando Hazelcast - Nó: " + nodeId + ", Porta: " + clusterPort);

        Config config = new Config();

        config.setClusterName("avatar-cluster");
        config.setInstanceName("avatar-node-" + nodeId);

        config.setProperty("hazelcast.network.heartbeat.interval.seconds", "3");

        config.setProperty("hazelcast.network.max.no.heartbeat.seconds", "10");

        config.setProperty("hazelcast.socket.connect.timeout.millis", "2000");

        config.setProperty("hazelcast.internal.member.connect.timeout.millis", "2000");
        config.setProperty("hazelcast.tcp.join.timeout.seconds", "1");

        config.setProperty("hazelcast.shutdownhook.enabled", "false");
        config.setProperty("hazelcast.phone.home.enabled", "false");

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(clusterPort)
                .setPortAutoIncrement(false);



        networkConfig.getJoin().getMulticastConfig().setEnabled(false);

        TcpIpConfig tcpIpConfig = networkConfig.getJoin().getTcpIpConfig();
        tcpIpConfig.setEnabled(true);

        if (memberList != null && !memberList.isEmpty()) {
            String[] members = memberList.split(",");
            for (String member : members) {
                tcpIpConfig.addMember(member.trim());
            }
        } else {
            System.err.println("❌ HAZELCAST_MEMBER_LIST não configurado. O cluster não se formará.");
        }

        if (publicAddress != null && !publicAddress.isEmpty()) {
            config.setProperty("hazelcast.local.publicAddress", publicAddress);
        }

        MapConfig leaderMapConfig = new MapConfig();
        leaderMapConfig.setName("leader-registry")
                .setTimeToLiveSeconds(30);

        MapConfig electionMapConfig = new MapConfig();
        electionMapConfig.setName("leader-election")
                .setTimeToLiveSeconds(30);

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