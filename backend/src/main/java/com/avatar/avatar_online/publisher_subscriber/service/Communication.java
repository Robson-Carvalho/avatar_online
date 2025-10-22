package com.avatar.avatar_online.publisher_subscriber.service;

import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class Communication {
    private final SimpMessagingTemplate messagingTemplate;

    public Communication(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToUser(String userSession, OperationResponseDTO operationResponseDTO) {
        messagingTemplate.convertAndSendToUser(userSession, "/queue/response", operationResponseDTO);
    }
}
