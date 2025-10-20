package com.avatar.avatar_online.service;

import com.avatar.avatar_online.network.Host;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PeerService {

    private final Set<String> peers = ConcurrentHashMap.newKeySet();
    private final RestTemplate restTemplate = new RestTemplate();

    public void addPeer(String ip) {
        if (!ip.equals(Host.getLocalIPv4()) && peers.add(ip)) {
            System.out.println("✅ Peer adicionado: " + ip);
            notifyNewPeer(ip);
        }
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void notifyNewPeer(String ip) {
        try {
            String url = "http://" + ip + ":8080/peers";

            Map<String, String> body = Map.of("ip", Host.getLocalIPv4());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(url, request, String.class);

            System.out.println("📤 Avisei ao host " + ip + " que eu existo.");
        } catch (Exception e) {
            System.err.println("❌ Falha ao avisar " + ip);
        }
    }
}
