package com.avatar.avatar_online.publisher_subscriber.component;

import com.avatar.avatar_online.publisher_subscriber.handlers.HandleDisconnected;
import com.avatar.avatar_online.publisher_subscriber.handlers.HandleGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final HandleDisconnected handleDisconnected;


    @Autowired
    public WebSocketEventListener(HandleDisconnected handleDisconnected) {
        this.handleDisconnected = handleDisconnected;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        System.out.println("❌ Cliente desconectado: " + sessionId);
        handleDisconnected.handleSessionDisconnect(sessionId, "");
    }
}
