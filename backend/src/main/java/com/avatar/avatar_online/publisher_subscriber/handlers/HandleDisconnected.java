package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.Truffle_Comunication.TruffleApiUser;
import com.avatar.avatar_online.game.Match;
import com.avatar.avatar_online.game.MatchManagementService;
import com.avatar.avatar_online.publisher_subscriber.handlers.records.PlayerInGame;
import com.avatar.avatar_online.publisher_subscriber.model.*;
import com.avatar.avatar_online.publisher_subscriber.service.Communication;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HandleDisconnected {
    private final Communication communication;
    private final MatchManagementService matchManagementService;
    private final HazelcastInstance hazelcast;

    private final RedirectService redirectService;

    private final IQueue<PlayerInGame> waitingQueue;

    private final ObjectMapper objectMapper;

    private final CardService cardService;

    private final OnlineUsers onlineUsers;

    private final TruffleApiUser truffleApiUser;


    @Autowired
    public HandleDisconnected(MatchManagementService matchManagementService, Communication communication,
                              @Qualifier("hazelcastInstance") HazelcastInstance hazelcast, RedirectService redirectService, ObjectMapper objectMapper, CardService cardService, OnlineUsers onlineUsers, TruffleApiUser truffleApiUser) {
        this.matchManagementService = matchManagementService;
        this.hazelcast = hazelcast;
        this.communication = communication;
        this.waitingQueue = hazelcast.getQueue("matchmaking-queue");
        this.redirectService = redirectService;
        this.objectMapper = objectMapper;
        this.cardService = cardService;
        this.onlineUsers = onlineUsers;
        this.truffleApiUser = truffleApiUser;
    }

    public OperationResponseDTO logout(OperationRequestDTO operation, String userSession) {
        String userID = (String) operation.getPayload().get("userID");

        try{
            this.handleSessionDisconnect(userSession, userID);
            onlineUsers.removeByUserId(userID);
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK,"Usuário desconectado com sucesso!",null);
        }catch(Exception e){
            return new OperationResponseDTO(operation.getOperationType(),OperationStatus.ERROR, "Erro inesperado: " + e.getMessage(), null);
        }
    }

    private Match getMatch(OperationRequestDTO operation){
        String matchID = (String) operation.getPayload().get("matchID");
        return matchManagementService.getMatchState(matchID);
    }

    public void surrender(OperationRequestDTO operation, String userSession) {
        Match match = this.getMatch(operation);

        if(match != null){
            String opponentSession = "";

            if(match.getPlayer1().getUserSession().equals(userSession)){
                opponentSession = match.getPlayer2().getUserSession();
            }else{
                opponentSession = match.getPlayer1().getUserSession();
            }

            System.out.println("Enviar STATUS para o oponente ["+opponentSession+"] que partida acabou com vitória!");
            this.sendToOpponentStatusWin(opponentSession, match);

            if(opponentSession.equals(match.getPlayer1().getUserSession())){
                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer1().getNickname());
            } else {
                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer2().getNickname());
            }

            matchManagementService.unregisterMatch(match.getMatchId());
        }
    }

    public void handleSessionDisconnect(String sessionId, String userID) {
        for (PlayerInGame player : waitingQueue) {
            if (player.getUserSession().equals(sessionId) || player.getUserId().equals(userID)) {
                System.out.println("🎯 Removendo player com sessionId: " + sessionId + " ou userID: "+ userID+" da fila");
                onlineUsers.removeBySessionId(sessionId);
                boolean remove = waitingQueue.remove(player);

                if (!remove){
                    System.out.println("Erro ao remover da fila");
                    return;
                }

                System.out.println("Payer removido da fila com sucesso!");
                return;
            }
        }

        System.out.println("Nenhuma jogador na fila com sessão ID: " + sessionId);
        this.checkDisconnectedPlayerState(sessionId, userID);
    }

    private void checkDisconnectedPlayerState(String sessionId, String userID) {
        String opponentSession = matchManagementService.getOpponentIfPlayerInMatch(sessionId, userID);

        if(!opponentSession.isEmpty()) {
            System.out.println("Enviar STATUS para o oponente ["+opponentSession+"] que partida acabou com vitória!");
            Match match =  matchManagementService.getMatchByPlayerID(sessionId, userID);

            this.sendToOpponentStatusWin(opponentSession, match);
            if(opponentSession.equals(match.getPlayer1().getUserSession())){
                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer1().getNickname());
            } else {
                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer2().getNickname());
            }
            matchManagementService.unRegisterMatchBySessionId(sessionId);
            onlineUsers.removeBySessionId(sessionId);
            return;
        }

        System.out.println("Nenhuma jogador em partida com sessão ID: " + sessionId + " ou com userID: " + userID);
    }

    private void sendToOpponentStatusWin(String userSession, Match match){
        if (match.getIslocalMatch()){
            // Aqui tem que verificar ainda, fiquei sem tempo
            OperationResponseDTO response = new OperationResponseDTO(OperationType.FINISHED_SURRENDER.toString(), OperationStatus.OK, "Você ganhou!", null);

            communication.sendToUser(userSession, response);
        } else {
            if (match.getPlayer1().getUserSession().equals(userSession)){
                OperationResponseDTO response = new OperationResponseDTO(OperationType.FINISHED_SURRENDER.toString(), OperationStatus.OK, "Você ganhou!", null);

                Map<String, Object> newPayload = new HashMap<>();

                newPayload.put("response", response);
                newPayload.put("userSession", userSession);

                OperationRequestDTO newOperation = new OperationRequestDTO(
                        OperationType.FINISHED_SURRENDER.toString(),
                        newPayload
                );

                redirectService.sendOperationRequestToNode(
                        match.getManagerNodeId(),
                        "FinishGameSurrender",
                        newOperation,
                        HttpMethod.POST
                );
            } else if (match.getPlayer2().getUserSession().equals(userSession)) {
                OperationResponseDTO response = new OperationResponseDTO(OperationType.FINISHED_SURRENDER.toString(), OperationStatus.OK, "Você ganhou!", null);

                Map<String, Object> newPayload = new HashMap<>();

                newPayload.put("response", response);
                newPayload.put("userSession", userSession);

                OperationRequestDTO newOperation = new OperationRequestDTO(
                        OperationType.FINISHED_SURRENDER.toString(),
                        newPayload
                );

                redirectService.sendOperationRequestToNode(
                        match.getPlayer2().getHostAddress(),
                        "FinishGameSurrender",
                        newOperation,
                        HttpMethod.POST
                );
            }
        }
    }

}
