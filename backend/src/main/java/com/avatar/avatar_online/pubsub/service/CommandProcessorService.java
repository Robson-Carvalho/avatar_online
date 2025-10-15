package com.avatar.avatar_online.pubsub.service;

import com.avatar.avatar_online.enums.ResponsePubSub;
import com.avatar.avatar_online.pubsub.ClientMessageDTO;
import com.avatar.avatar_online.pubsub.ServerEventDTO;
import com.avatar.avatar_online.pubsub.SignInDTO;
import com.avatar.avatar_online.pubsub.SignUpDTO;
import com.avatar.avatar_online.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class CommandProcessorService {

    private static final String KAFKA_TOPIC_OUT = "server-to-client";

    @Autowired
    private PublisherService publisherService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;

    @KafkaListener(topics = "client-to-server", groupId = "logic-group")
    public void processClientCommand(String messageJson, @Header(KafkaHeaders.RECEIVED_KEY) String clientId) {
        ServerEventDTO responseEvent;
        try {
            ClientMessageDTO messageDTO = objectMapper.readValue(messageJson, ClientMessageDTO.class);
            responseEvent = handleCommand(clientId, messageDTO);
        } catch (JsonProcessingException e) {
            System.err.println("CommandProcessor Error: Falha ao desserializar comando. " + e.getMessage());
            responseEvent = buildErrorResponse(clientId, "Comando mal formatado ou inválido.");
        } catch (Exception e) {
            System.err.println("CommandProcessor Error: Erro inesperado ao processar comando. " + e.getMessage());
            responseEvent = buildErrorResponse(clientId, "Erro interno no servidor.");
        }

        publishResponse(clientId, responseEvent);
    }

    private ServerEventDTO handleCommand(String clientId, ClientMessageDTO messageDTO) throws JsonProcessingException {
        String commandType = messageDTO.getCommandType();
        if (commandType == null) {
            return buildErrorResponse(clientId, "O tipo do comando não pode ser nulo.");
        }

        UUID userUuid;
        ResponsePubSub operation;

        switch (commandType) {
            case "signUp":
                SignUpDTO signUpDTO = objectMapper.readValue(messageDTO.getPayload(), SignUpDTO.class);
                userUuid = userService.signUp(signUpDTO);
                operation = ResponsePubSub.SIGNUP_RESPONSE;
                break;

            case "signIn":
                SignInDTO signInDTO = objectMapper.readValue(messageDTO.getPayload(), SignInDTO.class);
                userUuid = userService.signIn(signInDTO);
                operation = ResponsePubSub.LOGIN_RESPONSE;
                break;

            default:
                return buildErrorResponse(clientId, "Tipo de comando desconhecido: " + commandType);
        }

        if (userUuid != null) {
            Map<String, Object> successPayload = Map.of("Status", "OK", "userUUID", userUuid.toString());
            return buildResponse(clientId, operation, successPayload);
        } else {
            return buildErrorResponse(clientId, "Falha na operação: credenciais inválidas ou usuário já existente.");
        }
    }

    private void publishResponse(String clientId, ServerEventDTO event) {
        try {
            String responseJson = objectMapper.writeValueAsString(event);
            publisherService.publish(KAFKA_TOPIC_OUT, responseJson, clientId);
        } catch (JsonProcessingException e) {
            System.err.println("CommandProcessor FATAL: Falha ao serializar resposta do servidor. " + e.getMessage());
        }
    }

    private ServerEventDTO buildResponse(String clientId, ResponsePubSub responseType, Object payload) {
        ServerEventDTO serverEventDTO = new ServerEventDTO();
        serverEventDTO.setRecipientId(clientId);
        serverEventDTO.setEventType(responseType.toString());
        serverEventDTO.setData(payload);
        return serverEventDTO;
    }

    private ServerEventDTO buildErrorResponse(String clientId, String errorMessage) {
        Map<String, Object> errorPayload = Collections.singletonMap("error", errorMessage);
        return buildResponse(clientId, ResponsePubSub.ERROR_RESPONSE, errorPayload);
    }
}