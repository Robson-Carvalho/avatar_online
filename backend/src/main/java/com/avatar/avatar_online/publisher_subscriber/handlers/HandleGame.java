package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.game.Match;
import com.avatar.avatar_online.game.GameState;
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
import com.avatar.avatar_online.service.DeckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.collection.IQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class HandleGame {
    private final Communication communication;
    private final MatchManagementService matchManagementService;
    private final HazelcastInstance hazelcast;

    private final RedirectService redirectService;

    private final IQueue<PlayerInGame> waitingQueue;

    private final ObjectMapper objectMapper;

    private final CardService cardService;

    @Autowired
    public HandleGame(MatchManagementService matchManagementService, Communication communication,
                      @Qualifier("hazelcastInstance") HazelcastInstance hazelcast, RedirectService redirectService, ObjectMapper objectMapper, DeckService deckService, CardService cardService) {
        this.matchManagementService = matchManagementService;
        this.hazelcast = hazelcast;
        this.communication = communication;
        this.waitingQueue = hazelcast.getQueue("matchmaking-queue");
        this.redirectService = redirectService;
        this.objectMapper = objectMapper;

        this.cardService = cardService;
    }

    public void handleJoinInQueue(OperationRequestDTO operation, String userSession) {
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        String userID = (String) operation.getPayload().get("userID");

        List<Card> cards = cardService.getCardsInDeck(userID);

        if(cards.size() < 5){
            OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(), OperationStatus.WARNING, "Deck incompleto!", null);
            communication.sendToUser(userSession, response);
            return;
        }

        PlayerInGame player = new PlayerInGame(userID, userSession, currentNodeId);

        if(waitingQueue.contains(player)){
            OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(),OperationStatus.WARNING, "Você já está na fila!", null);
            communication.sendToUser(userSession, response);
            return;
        }

        try {
            PlayerInGame opponent = waitingQueue.poll();

            if (opponent != null) {
                List<Card> cardsOpponent = cardService.getCardsInDeck(opponent.getUserId());

                GameState newGame = new GameState(player.getUserId(), opponent.getUserId());
                newGame.getPlayerOne().setCards(cards);
                newGame.getPlayerTwo().setCards(cardsOpponent);

                Match match = new Match(currentNodeId, player, opponent, newGame);

                List<CardDTO> cardsDTO = new ArrayList<>();
                for (Card card : cards) {
                    cardsDTO.add(new CardDTO(card));
                }

                List<CardDTO> cardsOpponentDTO = new ArrayList<>();
                for (Card card : cardsOpponent) {
                    cardsOpponentDTO.add(new CardDTO(card));
                }

                GameStateDTO gameStateDTO = new GameStateDTO(newGame, cardsDTO, cardsOpponentDTO);
                MatchFoundResponseDTO matchDTO = new MatchFoundResponseDTO(match.getMatchId(), currentNodeId, gameStateDTO, player, opponent, match.isLocalMatch());

                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.MATCH_FOUND.toString(),
                        OperationStatus.OK,
                        "Partida encontrada",
                        matchDTO
                );

                matchManagementService.registerMatch(match);

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
        Match match = this.getMatch(operation);

        if(match == null){
            this.sendMessageNotFoundMath(userSession, operation);
            return;
        }

        if(match.getIslocalMatch()) {
            System.out.println("Jogo local " + userSession + " apertou play e chegou aqui");
            // processa e envia
        } else {
            // PS dica doq acho que tem que ser feito; Processa aqui a ação e dps envia ao outro nó com o código abaixo

            Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

            newPayload.put("userSession", userSession);

            OperationRequestDTO newOperation = new OperationRequestDTO(
                    operation.getOperationType(),
                    newPayload
            );

            if (!match.getManagerNodeId().equals(currentNodeId) ) {
                System.out.println("Jogo Distribuído, estou no servidor não gerenciador da partida " + userSession + " apertou play e chegou aqui");
                redirectService.sendOperationRequestToNode(
                        match.getManagerNodeId(),
                        "UpdateGame",
                        newOperation,
                        HttpMethod.POST
                );
            } else {
                System.out.println("Jogo Distribuído, estou no servidor gerenciador da partida " + userSession + " apertou play e chegou aqui");
                redirectService.sendOperationRequestToNode(
                        match.getPlayer2().getHostAddress(),
                        "UpdateGame",
                        newOperation,
                        HttpMethod.POST
                );
            }
        }
        // aplicar lógica no jogo e atualizar ambos.
    }

    public void ProcessPlayCardFromOtherNode(OperationRequestDTO operation, String userSession) {
        System.out.println("Carta jogada por: " + userSession);
        // --- Ideia do método ---
        // Se o nó for seguidor, apenas manda a atualização para o usuário
        // Se o nó for Líder, processa a ação enviada pelo outro jogador
    }

    public void handleActionActivateCard(OperationRequestDTO operation, String userSession) {
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        Match match = this.getMatch(operation);

        if(match == null){
            this.sendMessageNotFoundMath(userSession, operation);
            return;
        }

        if(match.getIslocalMatch()) {
            System.out.println("Jogo local " + userSession + " ativou carta e chegou aqui");
            // processa e envia
        } else {
            // PS dica doq acho que tem que ser feito; Processa aqui a ação e dps envia ao outro nó com o código abaixo

            Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

            newPayload.put("userSession", userSession);

            OperationRequestDTO newOperation = new OperationRequestDTO(
                    operation.getOperationType(),
                    newPayload
            );

            if (!match.getManagerNodeId().equals(currentNodeId) ) {
                System.out.println("Jogo Distribuído, estou no servidor não gerenciador da partida " + userSession + " ativei carta e chegou aqui");
                redirectService.sendOperationRequestToNode(
                        match.getManagerNodeId(),
                        "UpdateGameActiveCard",
                        newOperation,
                        HttpMethod.POST
                );
            } else {
                System.out.println("Jogo Distribuído, estou no servidor gerenciador da partida " + userSession + " ativei carta e chegou aqui");
                redirectService.sendOperationRequestToNode(
                        match.getPlayer2().getHostAddress(),
                        "UpdateGameActiveCard",
                        newOperation,
                        HttpMethod.POST
                );
            }
        }
        // aplicar lógica no jogo e atualizar ambos.
    }

    public void ProcessActiveCardFromOtherNode(OperationRequestDTO operation, String userSession) {
        System.out.println("Carta ativada por: " + userSession);
        // --- Ideia do método ---
        // Se o nó for seguidor, apenas manda a atualização para o usuário
        // Se o nó for Líder, processa a ação enviada pelo outro jogador
    }

    private Match getMatch(OperationRequestDTO operation){
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

    public OperationResponseDTO logout(OperationRequestDTO operation, String userSession) {
        String userID = (String) operation.getPayload().get("userID");

        try{
            this.handleSessionDisconnect(userSession, userID);
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK,"Usuário desconectado com sucesso!",null);
        }catch(Exception e){
            return new OperationResponseDTO(operation.getOperationType(),OperationStatus.ERROR, "Erro inesperado: " + e.getMessage(), null);
        }
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
            matchManagementService.unregisterMatch(match.getMatchId());
            this.sendToOpponentStatusWin(opponentSession);
        }
    }

    public void handleSessionDisconnect(String sessionId, String userID) {
        for (PlayerInGame player : waitingQueue) {
            if (player.getUserSession().equals(sessionId) || player.getUserId().equals(userID)) {
                System.out.println("🎯 Removendo player com sessionId: " + sessionId + " ou userID: "+ userID+" da fila");

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
            this.sendToOpponentStatusWin(opponentSession);
            matchManagementService.unRegisterMatchBySessionId(sessionId);
            return;
        }

        System.out.println("Nenhuma jogador em partida com sessão ID: " + sessionId + " ou com userID: " + userID);
    }

    private void sendToOpponentStatusWin(String userSession){
        OperationResponseDTO response = new OperationResponseDTO(OperationType.FINISHED_SURRENDER.toString(), OperationStatus.OK, "Você ganhou!", null);
        communication.sendToUser(userSession, response);
    }
}