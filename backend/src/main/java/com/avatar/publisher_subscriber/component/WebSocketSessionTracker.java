package com.avatar.publisher_subscriber.component;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionTracker implements ChannelInterceptor {

    public static final ConcurrentHashMap<String, String> sessionIdToUser = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() != null) {
            switch (accessor.getCommand()) {
                case CONNECT -> {
                    String sessionId = accessor.getSessionId();
                    System.out.println("🔗 Nova conexão: " + sessionId);
                    // Mapeia o sessionId para um identificador lógico (ou o próprio sessionId)
                    sessionIdToUser.put(sessionId, sessionId);
                }
                case DISCONNECT -> {
                    String sessionId = accessor.getSessionId();
                    System.out.println("❌ Desconectado: " + sessionId);
                    sessionIdToUser.remove(sessionId);
                }
            }
        }
        return message;
    }
}
