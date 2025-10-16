package com.avatar.avatar_online.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Habilita o processamento de mensagens WebSocket, com um broker por trás
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Define o endpoint que os clientes usarão para se conectar ao servidor WebSocket.
        // O SockJS é usado como uma alternativa para navegadores que não suportam WebSockets.
        registry.addEndpoint("/ws-connect").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Define o prefixo para os tópicos que os clientes podem se inscrever.
        // Mensagens do servidor para o cliente usarão este prefixo.
        // O "/user" é importante para mensagens diretas a um usuário específico.
        registry.enableSimpleBroker("/topic", "/user");

        // Define o prefixo que os clientes usarão para ENVIAR mensagens ao servidor.
        // Ex: Um cliente enviará para "/app/processar"
        registry.setApplicationDestinationPrefixes("/app");
    }
}