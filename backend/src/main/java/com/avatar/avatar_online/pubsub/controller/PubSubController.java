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
        String clientId = principal.getName();

        gatewayService.handleClientCommand(clientId, rawMessage);
    }
}
