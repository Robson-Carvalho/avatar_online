package com.avatar.avatar_online.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class WelcomeController {

    @GetMapping
    public  ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> welcomeData = Map.of(
                "application", "Avatar Online P2P Server",
                "description", "Servidor descentralizado para jogo multiplayer"
        );

        return ResponseEntity.ok(welcomeData);
    }
}
