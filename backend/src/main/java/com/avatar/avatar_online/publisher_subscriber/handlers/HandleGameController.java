package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.game.Match;
import com.avatar.avatar_online.game.MatchManagementService;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.GameStateDTO;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.MatchFoundResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.handlers.records.PlayerInGame;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationStatus;
import com.avatar.avatar_online.publisher_subscriber.model.OperationType;
import com.avatar.avatar_online.publisher_subscriber.service.Communication;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void ProcessActiveCardFromOtherNode(OperationRequestDTO operation, OperationResponseDTO opResponseDTO) {
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();

        Match match = this.getMatch(operation);

        if (match.getManagerNodeId().equals(currentNodeId)){
            communication.sendToUser(match.getPlayer1().getUserSession(), opResponseDTO);
        } else {

            communication.sendToUser(match.getPlayer2().getUserSession(), opResponseDTO);
        }

    }

    public void ProcessPlayCardFromOtherNode(OperationRequestDTO operation, OperationResponseDTO opResponseDTO) {
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();

        Match match = this.getMatch(operation);

        if (match.getManagerNodeId().equals(currentNodeId)){
            communication.sendToUser(match.getPlayer1().getUserSession(), opResponseDTO);
        } else {

            communication.sendToUser(match.getPlayer2().getUserSession(), opResponseDTO);
        }
    }

    private Match getMatch(OperationRequestDTO operation){
        String matchID = (String) operation.getPayload().get("matchID");
        return matchManagementService.getMatchState(matchID);
    }
}
