package com.avatar.avatar_online.pubsub.service;

import com.avatar.avatar_online.pubsub.ServerEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;

// PubSub Subscriber
@Service
public class CommandProcessorService {

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "client-to-server", groupId = "logic-group")
    public void processClientCommand(String message, @Header(KafkaHeaders.RECEIVED_KEY) String clientId) throws JsonProcessingException {

        System.out.println("Processing command for Client: " + clientId + " | Message: " + message);

        ServerEventDTO serverEventDTO = new ServerEventDTO();
        serverEventDTO.setRecipientId(clientId);
        serverEventDTO.setData("content type");
        serverEventDTO.setEventType("Coisas malucas");

        try {
            String response = objectMapper.writeValueAsString(serverEventDTO);
            System.out.println("oi mundiça");
            publisherService.publish("server-to-client", response, clientId);
        } catch (JsonProcessingException e) {
            System.out.println("error: " + e.getMessage());
        }
    }
}
