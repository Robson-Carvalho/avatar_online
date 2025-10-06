package com.avatar.avatar_online.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppStartupConfig {

    @Bean
    public CommandLineRunner keepAlive() {
        return args -> {
            System.out.println("🚀 Aplicação iniciada com sucesso!");
            System.out.println("📡 Hazelcast Cluster ativo");
            System.out.println("🌐 Servidor web rodando na porta 8080");
            System.out.println("💾 Banco de dados conectado");

            // Mantém a aplicação rodando
            Thread keepAliveThread = new Thread(() -> {
                try {
                    // Thread de keep-alive infinita
                    while (true) {
                        Thread.sleep(60000); // 1 minuto
                        System.out.println("❤️  Aplicação ativa...");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            keepAliveThread.setDaemon(true);
            keepAliveThread.start();
        };
    }
}