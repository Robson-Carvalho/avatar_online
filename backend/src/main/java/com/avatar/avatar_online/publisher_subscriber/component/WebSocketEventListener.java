package com.avatar.avatar_online.publisher_subscriber.component;

import com.avatar.avatar_online.publisher_subscriber.handlers.HandleDisconnected;
import com.avatar.avatar_online.publisher_subscriber.handlers.HandleGame;
import com.avatar.avatar_online.publisher_subscriber.model.OnlineUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final OnlineUsers onlineUsers;
    private final HandleDisconnected handleDisconnected;


    @Autowired
    public WebSocketEventListener(OnlineUsers onlineUsers, HandleDisconnected handleDisconnected) {
        this.onlineUsers = onlineUsers;
        this.handleDisconnected = handleDisconnected;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        System.out.println("❌ Cliente desconectado: " + sessionId);
        onlineUsers.removeBySessionId(sessionId);
        handleDisconnected.handleSessionDisconnect(sessionId, "");
    }
}
