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

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @Autowired
    private ObjectMapper objectMapper;

    public void handleClientCommand(String clientID, ClientMessageDTO RawMessage){
        try {
            String jsonMessage = objectMapper.writeValueAsString(RawMessage);
            System.out.println("Mensagem passou pelo gateway hein!");
            publisherService.publish("client-to-server", jsonMessage, clientID);
        } catch (JsonProcessingException e) {
            System.out.println("erro ao processar mensagem do cliente: " + e.getMessage());
        }
    }

    public void handleSignUp(ClientMessageDTO rawMessage, String principalId){
        try {
            String jsonMessage = objectMapper.writeValueAsString(rawMessage);
            publisherService.publish("client-to-server", jsonMessage, principalId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "server-to-client", groupId = "gateway-group")
    public void routeServerToCliente(String message) {
        try {
            ServerEventDTO event = objectMapper.readValue(message, ServerEventDTO.class);

            String id = event.getRecipientId().trim();
            String destiny = "/topic/update";

            System.out.println("Enviando mensagem para: " + id.trim());
            simpMessagingTemplate.convertAndSendToUser(id, destiny, message);

        } catch (JsonProcessingException e) {
            System.out.println("erro ao processar mensagem do servidor: " + e.getMessage());
        }
    }
}
