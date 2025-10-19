package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.raft.model.AppendEntriesRequest;
import com.avatar.avatar_online.raft.model.AppendEntriesResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class RedirectService {

    private final LeaderDiscoveryService leaderDiscoveryService;
    private final RestTemplate restTemplate;

    public RedirectService(@Lazy LeaderDiscoveryService leaderDiscoveryService,
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

    public void sendCommandToNode(String targetURL, Object command, HttpMethod method) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(command, headers);

            restTemplate.exchange(targetURL, method, requestEntity, String.class);
        } catch (Exception e) {
            System.err.println("❌ Falha ao enviar comando para " + targetURL + ": " + e.getMessage());
        }
    }

    public ResponseEntity<AppendEntriesResponse> sendCommandToNode(
            String targetURL,
            Object command,
            HttpMethod method,
            Class<AppendEntriesResponse> responseType) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(command, headers);

            ResponseEntity<AppendEntriesResponse> responseEntity = restTemplate.exchange(
                    targetURL,
                    method,
                    requestEntity,
                    responseType
            );

            return responseEntity;
        } catch (DataIntegrityViolationException e){
            System.out.println("✅ Idempotência ok: Comando de usuário já aplicado. Retornando sucesso.");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ Falha ao enviar comando para " + targetURL + ": " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
