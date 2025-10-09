package com.avatar.avatar_online.config;

import com.avatar.avatar_online.service.ClusterLeadershipService;
import com.hazelcast.internal.cluster.ClusterService;
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
            System.out.println("🚀 Aplicação iniciada com sucesso!");
            System.out.println("📡 Hazelcast Cluster ativo");
            System.out.println("🌐 Servidor web rodando na porta 8080");
            System.out.println("💾 Banco de dados conectado");

            clusterLeadershipService.init();
        };
    }
}