package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.service.PeerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/peers")
public class PeerController {

    private final PeerService peerService;

    public PeerController(PeerService peerService) {
        this.peerService = peerService;
    }

    @PostMapping
    public ResponseEntity<String> registerPeer(@RequestBody Map<String, String> body) {
        String ip = body.get("ip");
        peerService.addPeer(ip);
        return ResponseEntity.ok("Peer registrado: " + ip);
    }

    @GetMapping
    public Set<String> getPeers() {
        return peerService.getPeers();
    }
}
