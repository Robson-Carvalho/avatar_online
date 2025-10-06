package com.avatar.avatar_online.pubsub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Gateway -> Cliente
        config.enableSimpleBroker("/topic");

        // Cliente -> Controller
        config.setApplicationDestinationPrefixes("/app");

        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gateway-connect")
                .setHandshakeHandler(new DefaultHandshakeHandler(){
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes){
                        String clientID = request.getURI().getQuery().trim();
                        if (clientID == null){
                            clientID = "anon-" + UUID.randomUUID();
                        }
                        String finalClientID = clientID;
                        System.out.println("handshake URI: " + request.getURI());
                        System.out.println("Principal definido: " + finalClientID);
                        return () -> finalClientID;
                    }
                })
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*").withSockJS();
    }
}
