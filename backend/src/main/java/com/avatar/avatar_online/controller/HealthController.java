package com.avatar.avatar_online.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Avatar Online Cluster");
        health.put("message", "🚀 Serviço rodando normalmente");
        return health;
    }

    @GetMapping("/")
    public String home() {
        return """
            <html>
            <head><title>Avatar Online Cluster</title></head>
            <body>
                <h1>🚀 Avatar Online Cluster</h1>
                <p>Serviço de cluster distribuído rodando!</p>
                <ul>
                    <li><a href="/health">Health Check</a></li>
                    <li><a href="/api/cluster/status">Status do Cluster</a></li>
                    <li><a href="/api/cluster/leader">Informações do Líder</a></li>
                    <li><a href="/api/cluster/nodes">Nós do Cluster</a></li>
                </ul>
            </body>
            </html>
            """;
    }
}