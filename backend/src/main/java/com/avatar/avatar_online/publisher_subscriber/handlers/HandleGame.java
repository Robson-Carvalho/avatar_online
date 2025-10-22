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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.collection.IQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class HandleGame {
    private final Communication communication;
    private final MatchManagementService matchManagementService;
    private final HazelcastInstance hazelcast;

    private final RedirectService redirectService;

    private final IQueue<PlayerInGame> waitingQueue;

    @Autowired
    public HandleGame(MatchManagementService matchManagementService, Communication communication,
                      @Qualifier("hazelcastInstance") HazelcastInstance hazelcast, RedirectService redirectService) {
        this.matchManagementService = matchManagementService;
        this.hazelcast = hazelcast;
        this.communication = communication;
        this.waitingQueue = hazelcast.getQueue("matchmaking-queue");
        this.redirectService = redirectService;
    }

    public void handleJoinInQueue(OperationRequestDTO operation, String userSession) {
        String userID = (String) operation.getPayload().get("userID");

        try {
            String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();
            PlayerInGame player = new PlayerInGame(userID, userSession, currentNodeId);

            PlayerInGame opponent = waitingQueue.poll();

            if (opponent != null) {
                GameState newGame = new GameState(player.getUserId(), opponent.getUserId());

                Match match = new Match(currentNodeId, player, opponent, newGame);

                GameStateDTO gameStateDTO = new GameStateDTO(newGame);

                MatchFoundResponseDTO matchDTO = new MatchFoundResponseDTO(match.getMatchId(), currentNodeId, gameStateDTO, player, opponent);

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
            System.out.println(e.getStackTrace());
            OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Erro inesperado: " + e.getMessage(), null);
            communication.sendToUser(userSession, response);
        }
    }

    public void handleNotifyGameFound(OperationResponseDTO operation) {
        MatchFoundResponseDTO matchDTO = (MatchFoundResponseDTO) operation.getData();
        String sessionId = matchDTO.getPlayer2().getUserSession();

        communication.sendToUser(sessionId, operation);
    }

    public OperationResponseDTO handlePlayCard(OperationRequestDTO operation) {

        return new OperationResponseDTO();

    }

    public OperationResponseDTO handleActivateCard(OperationRequestDTO operation) {
        // Similar ao handlePlayCard
        return new OperationResponseDTO();
    }
}