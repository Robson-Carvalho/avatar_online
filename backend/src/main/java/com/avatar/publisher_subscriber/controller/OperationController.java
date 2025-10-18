package com.avatar.publisher_subscriber.controller;

import com.avatar.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.publisher_subscriber.handlers.HandleUser;
import com.avatar.publisher_subscriber.model.OperationStatus;
import com.avatar.publisher_subscriber.model.OperationType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
public class OperationController {
    private final HandleUser handleUser;
    private final SimpMessagingTemplate messagingTemplate;

    public OperationController(HandleUser handleUser, SimpMessagingTemplate messagingTemplate) {
        this.handleUser = handleUser;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/operation")
    @SendTo("/topic/public")
    public void handleOperation(@Payload OperationRequestDTO operation, Principal principal) {
        String userId = principal.getName();
        System.out.println("📥 Recebida operação de " + userId + ": " + operation.getOperationType());

        OperationType type = null;

        try {
            type = OperationType.valueOf(operation.getOperationType());
        } catch (IllegalArgumentException e) {
            OperationResponseDTO response = new OperationResponseDTO();
            response.setOperationStatus(OperationStatus.ERROR);
            response.setMessage("Operação inválida: " + operation.getOperationType());
            messagingTemplate.convertAndSendToUser(userId, "/queue/response", response);
            return;
        }

        switch (type) {
            case CREATE_USER:
                messagingTemplate.convertAndSendToUser(userId, "/queue/response", handleUser.handleCreateUser(operation));
                break;
            case LOGIN_USER:
                messagingTemplate.convertAndSendToUser(userId, "/queue/response",  handleUser.handleLoginUser(operation));
                break;
            default:
                OperationResponseDTO response = new OperationResponseDTO();
                response.setOperationStatus(OperationStatus.ERROR);
                response.setMessage("Operação não reconhecida: " + operation.getOperationType());
                messagingTemplate.convertAndSendToUser(userId, "/queue/response", response);
                break;
        }

    }
}


