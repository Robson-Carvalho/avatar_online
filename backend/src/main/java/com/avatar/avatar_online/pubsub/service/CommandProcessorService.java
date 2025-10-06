package com.avatar.avatar_online.pubsub.service;

import com.avatar.avatar_online.enums.ResponsePubSub;
import com.avatar.avatar_online.pubsub.ClientMessageDTO;
import com.avatar.avatar_online.pubsub.ServerEventDTO;
import com.avatar.avatar_online.pubsub.SignUpDTO;
import com.avatar.avatar_online.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// PubSub Subscriber
@Service
public class CommandProcessorService {

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    UUID user_UUID;

    String Kafka_channel = "server-to-client";

    @KafkaListener(topics = "client-to-server", groupId = "logic-group")
    public void processClientCommand(String message, @Header(KafkaHeaders.RECEIVED_KEY) String clientId){

        try{
            ClientMessageDTO messageDTO = objectMapper.readValue(message, ClientMessageDTO.class);
            String commandType = messageDTO.getCommandType();
            if(commandType.equals("signUp")){
                SignUpDTO signUpDTO = objectMapper.readValue(messageDTO.getPayload(), SignUpDTO.class);
                user_UUID = userService.signUpProcessment(signUpDTO);
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
            user_UUID = null;
        }

        if (user_UUID == null) {
            Map<String, Object> errorPayload = Collections.singletonMap("error", "Invalid command");
            try{
            String payloadJson = objectMapper.writeValueAsString(errorPayload);
            ServerEventDTO eventMessage = buildResponse(clientId, ResponsePubSub.ERROR_RESPONSE, payloadJson);

                String response = objectMapper.writeValueAsString(eventMessage);
                publisherService.publish(Kafka_channel, response, clientId);
            } catch (JsonProcessingException e){
                System.out.println("Error ao processar ServerEventDTO como string: " + e.getMessage());
            }
            return;
        }

        Map<String, Object> payload = Map.of(
                "Status", "OK",
                "clientId", user_UUID
        );

        try {
        String payloadJson = objectMapper.writeValueAsString(payload);
        ServerEventDTO eventMessage = buildResponse(clientId, ResponsePubSub.SIGNUP_RESPONSE, payloadJson);
            String response = objectMapper.writeValueAsString(eventMessage);
            publisherService.publish(Kafka_channel, response, clientId);
        } catch (JsonProcessingException e) {
            System.out.println("Error ao processar ServerEventDTO como string: " + e.getMessage());
        }
    }

    // Classe para padronizar a construção de Response
    public ServerEventDTO buildResponse(String clientId, ResponsePubSub responsePubSub, String payload) {
        ServerEventDTO serverEventDTO = new ServerEventDTO();
        serverEventDTO.setRecipientId(clientId);
        serverEventDTO.setEventType(responsePubSub.toString());
        serverEventDTO.setData(payload);
        return serverEventDTO;
    }
}
