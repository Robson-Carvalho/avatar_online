package com.avatar.publisher_subscriber.controller;

import com.avatar.publisher_subscriber.model.OperationMessage;
import com.avatar.publisher_subscriber.model.ResponseMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class OperationController {

    private final SimpMessagingTemplate messagingTemplate;

    public OperationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/operation")
    @SendTo("/topic/public")
    public void handleOperation(@Payload OperationMessage message, Principal principal) {
        String userId = principal.getName();
        System.out.println("📥 Recebida operação de " + userId + ": " + message.getOperation());

        // Lógica simulada

        String result = "Servidor processou a operação: " + message.getOperation();

        // Envia resposta só para o usuário que enviou
        ResponseMessage response = new ResponseMessage("OK", result);

        messagingTemplate.convertAndSendToUser(userId, "/queue/response", response);
    }
}
