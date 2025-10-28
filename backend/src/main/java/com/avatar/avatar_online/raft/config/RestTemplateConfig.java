package com.avatar.avatar_online.raft.config;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager; // Importa o Manager
import org.apache.hc.client5.http.config.ConnectionConfig;

@Configuration
public class RestTemplateConfig {

    // Use variáveis de configuração reais aqui (ou @Value)
    private static final int MAX_TOTAL_CONNECTIONS = 300;
    private static final int MAX_CONN_PER_ROUTE = 100;

    @Bean
    public RestTemplate restTemplate() {

        // 1. Cria o Connection Manager com Pooling
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        // Define as regras de Pool global
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        // Define as regras de Pool por rota (por host/porta, ou seja, cada nó do Raft)
        connectionManager.setDefaultMaxPerRoute(MAX_CONN_PER_ROUTE);

        // 2. Cria o Cliente HTTP do Apache usando o Connection Manager
        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        // 3. Cria a Factory do Spring usando o cliente Apache
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        // Configuração de Timeout (Mantenha o que você já tinha)
        // factory.setConnectTimeout(2000);
        // factory.setReadTimeout(20000);

        // 4. Cria e retorna o RestTemplate
        return new RestTemplate(factory);
    }
}