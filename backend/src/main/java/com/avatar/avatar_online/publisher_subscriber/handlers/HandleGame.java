package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.game.Match;
import com.avatar.avatar_online.game.GameState;
import com.avatar.avatar_online.game.MatchManagementService;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.GameStateDTO;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.MatchFoundResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.handlers.records.PlayerInGame;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationStatus;

import com.avatar.avatar_online.publisher_subscriber.model.OperationType;
import com.avatar.avatar_online.publisher_subscriber.service.Communication;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.collection.IQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HandleGame {
    private final Communication communication;
    private final MatchManagementService matchManagementService;
    private final HazelcastInstance hazelcast;

    private final RedirectService redirectService;

    private final IQueue<PlayerInGame> waitingQueue;

    private final ObjectMapper objectMapper;

    @Autowired
    public HandleGame(MatchManagementService matchManagementService, Communication communication,
                      @Qualifier("hazelcastInstance") HazelcastInstance hazelcast, RedirectService redirectService, ObjectMapper objectMapper) {
        this.matchManagementService = matchManagementService;
        this.hazelcast = hazelcast;
        this.communication = communication;
        this.waitingQueue = hazelcast.getQueue("matchmaking-queue");
        this.redirectService = redirectService;
        this.objectMapper = objectMapper;
    }

    public void handleJoinInQueue(OperationRequestDTO operation, String userSession) {
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        String userID = (String) operation.getPayload().get("userID");

        PlayerInGame player = new PlayerInGame(userID, userSession, currentNodeId);

        if(waitingQueue.contains(player)){
            OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(),OperationStatus.WAITING, "Você já está na fila", null);
            communication.sendToUser(userSession, response);
            return;
        }

        try {
            PlayerInGame opponent = waitingQueue.poll();

            if (opponent != null) {
                GameState newGame = new GameState(player.getUserId(), opponent.getUserId());

                Match match = new Match(currentNodeId, player, opponent, newGame);

                GameStateDTO gameStateDTO = new GameStateDTO(newGame);

                MatchFoundResponseDTO matchDTO = new MatchFoundResponseDTO(match.getMatchId(), currentNodeId, gameStateDTO, player, opponent, match.isLocalMatch());

                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.MATCH_FOUND.toString(),
                        OperationStatus.OK,
                        "Partida encontrada",
                        matchDTO
                );

                matchManagementService.registerMatch(matchDTO);

                System.out.println("Partida formada: " + player.getUserId() + " vs " + opponent.getUserId());
                if(match.isLocalMatch()){
                    communication.sendToUser(match.getPlayer1().getUserSession(), response);
                    communication.sendToUser(match.getPlayer2().getUserSession(), response);
                    return;
                }

                communication.sendToUser(match.getPlayer1().getUserSession(), response);
                redirectService.sendOperationToNode(opponent.getHostAddress(), "GameFound", response, HttpMethod.POST);
            } else {
                System.out.println("Jogador " + player.getUserId() + " entrou na fila.");
                waitingQueue.add(player);

                OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Você entrou na fila", null);

                communication.sendToUser(userSession, response);
            }
        } catch (Exception e) {
            OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Erro inesperado: " + e.getMessage(), null);
            communication.sendToUser(userSession, response);
        }
    }

    public void handleActionPlayCard(OperationRequestDTO operation, String userSession) {
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        MatchFoundResponseDTO match = this.getMatch(operation);

        if(match == null){
            this.sendMessageNotFoundMath(userSession, operation);
            return;
        }

        if(match.getIslocalmatch()) {
            System.out.println("Jogo local" + userSession + "apertou play e chegou aqui");
        } else {
            Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

            newPayload.put("userSession", userSession);

            OperationRequestDTO newOperation = new OperationRequestDTO(
                    operation.getOperationType(),
                    newPayload
            );

            if (!match.getManagerNodeId().equals(currentNodeId)) {
                System.out.println("Jogo Distribuído, estou no servidor não gerenciador da partida" + userSession + "apertou play e chegou aqui");
                redirectService.sendOperationRequestToNode(
                        match.getManagerNodeId(),
                        "UpdateGame",
                        newOperation,
                        HttpMethod.POST
                );
            } else {
                System.out.println("Jogo Distribuído, estou no servidor gerenciador da partida" + userSession + "apertou play e chegou aqui");
            }
        }
        // aplicar lógica no jogo e atualizar ambos.
    }

    public void handleActionActivateCard(OperationRequestDTO operation, String userSession) {
        MatchFoundResponseDTO match = this.getMatch(operation);

        if(match == null){
           this.sendMessageNotFoundMath(userSession, operation);
            return;
        }

        // aplicar lógica no jogo e atualizar ambos.
    }

    private MatchFoundResponseDTO getMatch(OperationRequestDTO operation){
        String matchID = (String) operation.getPayload().get("matchID");
        return matchManagementService.getMatchState(matchID);
    }

    private void sendMessageNotFoundMath(String userSession, OperationRequestDTO operation){
        OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(),OperationStatus.ERROR, "Partida não encontrada!", null);
        communication.sendToUser(userSession, response);
    }

    public void handleNotifyGameFound(OperationResponseDTO operation) {

        MatchFoundResponseDTO matchDTO = this.objectMapper.convertValue(
                operation.getData(),
                MatchFoundResponseDTO.class
        );
        String sessionId = matchDTO.getPlayer2().getUserSession();

        communication.sendToUser(sessionId, operation);
    }
}