package com.avatar.avatar_online.publisher_subscriber.controller;

import com.avatar.avatar_online.publisher_subscriber.handlers.HandleCard;
import com.avatar.avatar_online.publisher_subscriber.handlers.HandleDeck;
import com.avatar.avatar_online.publisher_subscriber.handlers.HandleGame;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.handlers.HandleUser;
import com.avatar.avatar_online.publisher_subscriber.model.OperationStatus;
import com.avatar.avatar_online.publisher_subscriber.model.OperationType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
public class OperationController {
    private final HandleUser handleUser;
    private final HandleCard handleCard;
    private final HandleDeck handleDeck;
    private final HandleGame handleGame;
    private final SimpMessagingTemplate messagingTemplate;

    public OperationController(HandleUser handleUser, HandleCard handleCard, SimpMessagingTemplate messagingTemplate, HandleDeck handleDeck, HandleGame handleGame) {
        this.handleUser = handleUser;
        this.handleCard = handleCard;
        this.messagingTemplate = messagingTemplate;
        this.handleDeck = handleDeck;
        this.handleGame = handleGame;
    }

    @MessageMapping("/operation")
    @SendTo("/topic/public")
    public void handleOperation(@Payload OperationRequestDTO operation, Principal principal) {
        String userSession = principal.getName();
        System.out.println("📥 Recebida operação de " + userSession + ": " + operation.getOperationType());

        OperationType type;

        try {
            type = OperationType.valueOf(operation.getOperationType());
        } catch (IllegalArgumentException e) {
            OperationResponseDTO response = new OperationResponseDTO();
            response.setOperationStatus(OperationStatus.ERROR);
            response.setMessage("Operação inválida: " + operation.getOperationType());
            messagingTemplate.convertAndSendToUser(userSession, "/queue/response", response);
            return;
        }

        switch (type) {
            case AUTH_USER:
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response", handleUser.handleAuthUser(operation));
                break;
            case CREATE_USER:
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response", handleUser.handleCreateUser(operation));
                break;
            case LOGIN_USER:
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response",  handleUser.handleLoginUser(operation));
                break;
            case OPEN_PACKAGE:
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response", handleCard.handleOpenPackage(operation));
                break;
            case GET_DECK:
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response", handleDeck.handleGetDeck(operation));
                break;
            case GET_CARDS:
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response", handleCard.handleGetCards(operation));
                break;
            case UPDATE_DECK:
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response", handleDeck.handleUpdateDeck(operation));
                break;
            case JOIN_QUEUE:
                handleGame.handleJoinInQueue(operation, userSession);
                break;
            default:
                OperationResponseDTO response = new OperationResponseDTO();
                response.setOperationStatus(OperationStatus.ERROR);
                response.setMessage("Operação não reconhecida: " + operation.getOperationType());
                messagingTemplate.convertAndSendToUser(userSession, "/queue/response", response);
                break;
        }

    }
}


