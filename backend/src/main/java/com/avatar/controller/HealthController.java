package com.avatar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("🚦 status", "UP");
        health.put("⏰ timestamp", System.currentTimeMillis());
        health.put("🖥️ service", "Avatar Online Cluster");
        health.put("💬 message", "🚀 Serviço rodando normalmente");
        health.put("📊 clusterNodes", "Verifique /api/cluster/nodes para detalhes");
        health.put("👑 leaderInfo", "Verifique /api/cluster/leader para detalhes");
        return health;
    }

    @GetMapping("/")
    public String home() {
        return """
        <html>
        <head>
            <title>🚀 Avatar Online Cluster</title>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #f4f6f8;
                    color: #333;
                    margin: 0;
                    padding: 0;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    min-height: 100vh;
                }
                header {
                    background-color: #4CAF50;
                    color: white;
                    width: 100%;
                    padding: 20px 0;
                    text-align: center;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                main {
                    display: flex;
                    flex-wrap: wrap;
                    justify-content: center;
                    padding: 40px 20px;
                    gap: 20px;
                    width: 100%;
                    max-width: 1200px;
                }
                .card {
                    background-color: white;
                    border-radius: 10px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    padding: 20px;
                    width: 250px;
                    transition: transform 0.2s, box-shadow 0.2s;
                    text-align: left;
                }
                .card:hover {
                    transform: translateY(-5px);
                    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                }
                .card h2 {
                    margin-top: 0;
                    font-size: 1.2rem;
                }
                .card p {
                    font-size: 0.95rem;
                    margin-bottom: 10px;
                }
                .card a {
                    text-decoration: none;
                    color: #4CAF50;
                    font-weight: bold;
                    font-size: 0.95rem;
                }
                footer {
                    margin-top: auto;
                    padding: 15px 0;
                    width: 100%;
                    text-align: center;
                    font-size: 0.9rem;
                    color: #777;
                    border-top: 1px solid #ddd;
                    background-color: #fafafa;
                }
            </style>
        </head>
        <body>
            <header>
                <h1>🚀 Avatar Online Cluster</h1>
            </header>
            <main>
                <div class="card">
                    <h2>🚦 Health Check</h2>
                    <p>Verifica se o serviço está ativo e retorna status, timestamp e mensagens de saúde.</p>
                    <a href="/health">Ir para /health</a>
                </div>
                <div class="card">
                    <h2>📊 Cluster Status</h2>
                    <p>Mostra informações gerais do cluster, como líder, tamanho do cluster, nó atual e líder registrado.</p>
                    <a href="/api/cluster/status">Ir para /api/cluster/status</a>
                </div>
                <div class="card">
                    <h2>👑 Leader Info</h2>
                    <p>Detalhes do líder atual: nodeId, host, porta, endereço HTTP, último heartbeat e se está expirado.</p>
                    <a href="/api/cluster/leader">Ir para /api/cluster/leader</a>
                </div>
                <div class="card">
                    <h2>🖥️ Cluster Nodes</h2>
                    <p>Informações sobre os nós do cluster: total de nós e dados do nó atual.</p>
                    <a href="/api/cluster/nodes">Ir para /api/cluster/nodes</a>
                </div>
                <div class="card">
                    <h2>⚡ Sync Status</h2>
                    <p>Status da sincronização de dados: último sync, se há dados exportáveis e se este nó é líder.</p>
                    <a href="/api/cluster/sync/status">Ir para /api/cluster/sync/status</a>
                </div>
                <div class="card">
                    <h2>🏛️ Election Info</h2>
                    <p>Informações da eleição atual: líder da eleição, node atual, se há líder ativo e tamanho do cluster.</p>
                    <a href="/api/cluster/election/info">Ir para /api/cluster/election/info</a>
                </div>
            </main>
            <footer>
                &copy; 2025 Avatar Online Cluster
            </footer>
        </body>
        </html>
        """;
    }


}