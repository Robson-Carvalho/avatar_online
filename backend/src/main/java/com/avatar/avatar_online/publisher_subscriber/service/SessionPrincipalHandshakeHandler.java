package com.avatar.avatar_online.publisher_subscriber.service;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class SessionPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Object sessionIdAttr = attributes.get("sessionId");
        String sessionId = sessionIdAttr != null
                ? sessionIdAttr.toString()
                : UUID.randomUUID().toString();

        System.out.println("🔗 Nova conexão (Principal): " + sessionId);

        return () -> sessionId;
    }
}
