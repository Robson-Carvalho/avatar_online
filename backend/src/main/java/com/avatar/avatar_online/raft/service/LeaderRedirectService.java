package com.avatar.avatar_online.raft.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class LeaderRedirectService {

    private final LeaderDiscoveryService leaderDiscoveryService;
    private final RestTemplate restTemplate;

    public LeaderRedirectService(LeaderDiscoveryService leaderDiscoveryService,
                                 RestTemplate restTemplate) {
        this.leaderDiscoveryService = leaderDiscoveryService;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> redirectToLeader(String path, Object body, HttpMethod method) {
        try {
            Optional<String> leaderAddress = leaderDiscoveryService.getLeaderHttpAddress();

            if (leaderAddress.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("{\"error\": \"Líder não encontrado no cluster. Tente novamente.\"}");
            }

            String leaderUrl = leaderAddress.get() + path;
            System.out.println("🔄 Redirecionando requisição para o líder: " + leaderUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    leaderUrl, method, requestEntity, String.class
            );

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\": \"Falha ao redirecionar para o líder: " + e.getMessage() + "\"}");
        }
    }
}
