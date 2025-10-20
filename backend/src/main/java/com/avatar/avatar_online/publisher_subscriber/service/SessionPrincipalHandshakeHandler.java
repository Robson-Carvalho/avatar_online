package com.avatar.avatar_online.publisher_subscriber.service;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class SessionPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Verifica se o Spring já gerou um ID de sessão WebSocket (SockJS armazena em atributos)
        Object sessionIdAttr = attributes.get("sessionId");

        // Usa o sessionId real, se existir. Caso contrário, gera um fallback UUID.
        String sessionId = sessionIdAttr != null ? sessionIdAttr.toString() : UUID.randomUUID().toString();

        System.out.println("🔗 Nova conexão: " + sessionId);

        // Retorna um Principal cujo nome é o sessionId real — o que será usado por convertAndSendToUser()
        return () -> sessionId;
    }
}
