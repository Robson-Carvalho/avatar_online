package com.avatar.avatar_online.config;

import com.hazelcast.core.HazelcastInstance;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShutdownConfig {

    @Qualifier("hazelcastInstance")
    @Autowired(required = false)
    private HazelcastInstance hazelcastInstance;

    @PreDestroy
    public void onShutdown() {
        System.out.println("🛑 Desligando aplicação...");
        if (hazelcastInstance != null) {
            System.out.println("🛑 Desligando Hazelcast...");
            hazelcastInstance.shutdown();
        }
    }
}