package com.avatar.raft.config;

import com.avatar.raft.service.ClusterLeadershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppStartupConfig {

    @Autowired
    private ClusterLeadershipService clusterLeadershipService;

    @Bean
    public CommandLineRunner keepAlive() {
        return args -> {
            clusterLeadershipService.init();
        };
    }
}