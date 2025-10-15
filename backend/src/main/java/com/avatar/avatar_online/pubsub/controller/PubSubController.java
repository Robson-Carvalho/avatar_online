package com.avatar.avatar_online.pubsub.controller;

import com.avatar.avatar_online.pubsub.ClientMessageDTO;
import com.avatar.avatar_online.pubsub.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class PubSubController {

    @Autowired
    private GatewayService gatewayService;

    /**
     * **[REATORADO]** Endpoint único para todos os comandos vindos do cliente.
     */
    @MessageMapping("/command")
    public void receiveCommand(@Payload ClientMessageDTO message, Principal principal) {
        // **[SEGURANÇA]** Garante que a mensagem só será processada se houver um usuário (Principal) autenticado na sessão WebSocket.
        if (principal == null || principal.getName() == null) {
            System.err.println("WARN: Mensagem recebida sem um Principal válido. Mensagem descartada.");
            return;
        }

        String principalId = principal.getName();
        System.out.println("Comando '" + message.getCommandType() + "' recebido de: " + principalId);

        gatewayService.forwardClientMessageToKafka(message, principalId);
    }
}