package com.avatar.avatar_online.pubsub.service;

import com.avatar.avatar_online.pubsub.ClientMessageDTO;
import com.avatar.avatar_online.pubsub.ServerEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {
    private static final String TOPIC_CLIENT_TO_SERVER = "client-to-server";
    private static final String TOPIC_SERVER_TO_CLIENT = "server-to-client";
    private static final String WEBSOCKET_USER_DESTINATION = "/topic/update";

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void forwardClientMessageToKafka(ClientMessageDTO message, String principalId) {
        try {
            // Garante que o ID do cliente no DTO é o da sessão segura, e não um enviado pelo cliente.
            message.setClientID(principalId);
            String jsonMessage = objectMapper.writeValueAsString(message);
            publisherService.publish(TOPIC_CLIENT_TO_SERVER, jsonMessage, principalId);
        } catch (JsonProcessingException e) {
            System.err.println("Gateway Error: Falha ao serializar mensagem do cliente. " + e.getMessage());
            // Opcional: Enviar uma mensagem de erro de volta ao cliente.
        }
    }

    @KafkaListener(topics = TOPIC_SERVER_TO_CLIENT, groupId = "gateway-group")
    public void routeServerEventToClient(String serverEventJson) {
        try {
            ServerEventDTO event = objectMapper.readValue(serverEventJson, ServerEventDTO.class);
            String recipientId = event.getRecipientId();

            if (recipientId != null && !recipientId.trim().isEmpty()) {
                System.out.println("Gateway: Roteando evento '" + event.getEventType() + "' para o cliente: " + recipientId);
                // Envia para o destino privado do usuário
                simpMessagingTemplate.convertAndSendToUser(recipientId, WEBSOCKET_USER_DESTINATION, serverEventJson);
            } else {
                System.err.println("Gateway Warning: Mensagem do servidor recebida sem um recipientId. Descartando.");
            }
        } catch (JsonProcessingException e) {
            System.err.println("Gateway Error: Falha ao desserializar evento do servidor. " + e.getMessage());
        }
    }
}