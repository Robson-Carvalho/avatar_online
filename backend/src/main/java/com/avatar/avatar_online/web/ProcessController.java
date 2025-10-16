package com.avatar.avatar_online.web;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ProcessController {

    /**
     * Este método é acionado quando um cliente envia uma mensagem para "/app/processar".
     * A anotação @SendToUser direciona a resposta APENAS para o cliente que enviou a mensagem.
     * O Spring automaticamente resolve o destino para algo como "/user/{sessionId}/queue/reply".
     *
     * @param message A mensagem recebida do cliente.
     * @param headerAccessor Permite acessar informações da sessão, como o ID da sessão.
     * @return A resposta que será enviada de volta ao cliente.
     */
    @MessageMapping("/processar")
    @SendToUser("/queue/reply")
    public ServerResponse processMessage(@Payload ClientMessage message, SimpMessageHeaderAccessor headerAccessor) {
        // Obtém o ID da sessão do cliente que enviou a mensagem
        String sessionId = headerAccessor.getSessionId();

        System.out.println("Recebido de " + message.getFrom() + " (Sessão: " + sessionId + "): " + message.getContent());

        // Processa a informação...
        String responseContent = "Olá, " + message.getFrom() + "! Sua mensagem '" + message.getContent() + "' foi processada às " + LocalDateTime.now() + ".";

        return new ServerResponse(responseContent, sessionId);
    }
}