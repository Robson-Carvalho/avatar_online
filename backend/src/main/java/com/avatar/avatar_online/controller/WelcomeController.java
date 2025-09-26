package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avatar.avatar_online.util.ResponseBuilder;

import java.util.Map;

@RestController
@RequestMapping("/")
public class WelcomeController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> welcome() {
        Map<String, Object> welcomeData = Map.of(
                "application", "Avatar Online P2P Server",
                "description", "Servidor descentralizado para jogo multiplayer",
                "version", "1.0.0",
                "status", "online"
        );

        return ResponseBuilder.ok("Bem-vindo ao Avatar Online P2P", welcomeData);
    }
}
