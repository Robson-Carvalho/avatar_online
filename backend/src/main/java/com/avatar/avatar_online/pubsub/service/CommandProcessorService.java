package com.avatar.avatar_online.pubsub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

// PubSub Subscriber
@Service
public class CommandProcessorService {

    @Autowired
    private PublisherService publisherService; // Para responder ao cliente (publicando em server-events)

    @KafkaListener(topics = "client-to-server", groupId = "logic-group")
    public void processClientCommand(String message, @Header(KafkaHeaders.RECEIVED_KEY) String clientId) {

        System.out.println("Processing command for Client: " + clientId + " | Message: " + message);

        String response = "{ \"Header ID\": \"" + clientId + "\", \"status\": \"Success\", \"payload\": \"Conteúdo_response\"}";
        publisherService.publish("server-to-client", response, clientId);

    }
}
