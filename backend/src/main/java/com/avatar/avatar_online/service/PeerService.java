package com.avatar.avatar_online.service;

import com.avatar.avatar_online.network.Host;
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
            restTemplate.postForEntity(url, body, String.class);
            System.out.println("📤 Avisei " + ip + " que eu existo.");
        } catch (Exception e) {
            System.err.println("❌ Falha ao avisar " + ip);
        }
    }
}
