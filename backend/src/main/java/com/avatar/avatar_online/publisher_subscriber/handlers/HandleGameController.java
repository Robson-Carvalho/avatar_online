package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.game.MatchManagementService;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.MatchFoundResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.handlers.records.PlayerInGame;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.service.Communication;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class HandleGameController {
    private final Communication communication;
    private final MatchManagementService matchManagementService;
    private final HazelcastInstance hazelcast;

    private final RedirectService redirectService;

    private final IQueue<PlayerInGame> waitingQueue;

    private final ObjectMapper objectMapper;

    private final CardService cardService;

    @Autowired
    public HandleGameController(MatchManagementService matchManagementService, Communication communication,
                              @Qualifier("hazelcastInstance") HazelcastInstance hazelcast, RedirectService redirectService, ObjectMapper objectMapper, CardService cardService) {
        this.matchManagementService = matchManagementService;
        this.hazelcast = hazelcast;
        this.communication = communication;
        this.waitingQueue = hazelcast.getQueue("matchmaking-queue");
        this.redirectService = redirectService;
        this.objectMapper = objectMapper;
        this.cardService = cardService;
    }

    public void handleNotifyGameFound(OperationResponseDTO operation) {

        MatchFoundResponseDTO matchDTO = this.objectMapper.convertValue(
                operation.getData(),
                MatchFoundResponseDTO.class
        );
        String sessionId = matchDTO.getPlayer2().getUserSession();

        communication.sendToUser(sessionId, operation);
    }

    public void ProcessActiveCardFromOtherNode(OperationRequestDTO operation, String userSession) {
        System.out.println("Carta ativada por: " + userSession);
        // --- Ideia do método ---
        // Se o nó for seguidor, apenas manda a atualização para o usuário
        // Se o nó for Líder, processa a ação enviada pelo outro jogador
    }

    public void ProcessPlayCardFromOtherNode(OperationRequestDTO operation, String userSession) {
        System.out.println("Carta jogada por: " + userSession);
        // --- Ideia do método ---
        // Se o nó for seguidor, apenas manda a atualização para o usuário
        // Se o nó for Líder, processa a ação enviada pelo outro jogador
    }

}
