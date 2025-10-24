package com.avatar.avatar_online.publisher_subscriber.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class SessionIdHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String path = request.getURI().getPath();
        String[] parts = path.split("/");

        String sessionId = null;
        if (parts.length >= 2) {
            sessionId = parts[parts.length - 2];
        }

        if (sessionId != null && !sessionId.isEmpty()) {
            attributes.put("sessionId", sessionId);
            System.out.println("🧩 Interceptor capturou sessionId: " + sessionId);
        } else {
            System.out.println("⚠️ Não foi possível capturar sessionId do path: " + path);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
    }
}
