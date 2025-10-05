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

    @MessageMapping("/command")
    public void receiveCommand(@Payload ClientMessageDTO rawMessage, Principal principal) {

        System.out.println("Received message: " + rawMessage);

        String clientId;

        if (principal != null) {
            clientId = principal.getName();
        } else {
            System.out.println("WARN: Comando recebido sem usuário autenticado. Usando ID de convidado.");
            clientId = "GUEST_" + Thread.currentThread().threadId();
        }


        System.out.println("Enviando pro gateway hein");
        gatewayService.handleClientCommand(clientId, rawMessage);
    }
}
