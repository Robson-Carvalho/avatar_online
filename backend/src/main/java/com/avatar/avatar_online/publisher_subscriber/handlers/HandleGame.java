package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.HistoryResponseDTO;
import com.avatar.avatar_online.Truffle_Comunication.TruffleApiUser;
import com.avatar.avatar_online.game.Match;
import com.avatar.avatar_online.game.GameState;
import com.avatar.avatar_online.game.MatchManagementService;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.User;
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
import com.avatar.avatar_online.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.collection.IQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class HandleGame {
    private final Communication communication;
    private final MatchManagementService matchManagementService;
    private final HazelcastInstance hazelcast;

    private final RedirectService redirectService;

    private final IQueue<PlayerInGame> waitingQueue;

    private final ObjectMapper objectMapper;

    private final CardService cardService;
    private final UserService userService;
    private final TruffleApiUser truffleApiUser;

    @Autowired
    public HandleGame(MatchManagementService matchManagementService, Communication communication,
                      @Qualifier("hazelcastInstance") HazelcastInstance hazelcast, RedirectService redirectService, ObjectMapper objectMapper, CardService cardService, UserService userService, TruffleApiUser truffleApiUser) {
        this.matchManagementService = matchManagementService;
        this.hazelcast = hazelcast;
        this.communication = communication;
        this.waitingQueue = hazelcast.getQueue("matchmaking-queue");
        this.redirectService = redirectService;
        this.objectMapper = objectMapper;

        this.cardService = cardService;
        this.userService = userService;
        this.truffleApiUser = truffleApiUser;
    }

    public void handleGetHistoriyBlockchain(OperationRequestDTO operation, String userSession){

        ResponseEntity<String> response = userService.getHistory();

        if (response == null || response.getBody() == null) {
            OperationResponseDTO responseDTO = new OperationResponseDTO();
            responseDTO.setOperationStatus(OperationStatus.ERROR);
            responseDTO.setMessage("Operação não reconhecida: " + operation.getOperationType());
            communication.sendToUser(userSession, responseDTO);
        }

        OperationResponseDTO responseDTO = new OperationResponseDTO(
                operation.getOperationType(), OperationStatus.OK, "Guga Guga Guga", response
        );

        communication.sendToUser(userSession, responseDTO);
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

        Optional<User> userOptional = userService.findById(UUID.fromString(userID));
        if(userOptional.isEmpty()){
            System.out.println("Ta errado em playerInGame");
            return;
        }

        User user = userOptional.get();

        PlayerInGame player = new PlayerInGame(userID, userSession, currentNodeId, user.getNickname());

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
        String userID = (String) operation.getPayload().get("userID");

        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        Match match = this.getMatch(operation);

        if(match == null){
            this.sendMessageNotFoundMath(userSession, operation);
            return;
        }

        if(match.getIslocalMatch()) {
            match.playCard(userID);
            MatchFoundResponseDTO matchDTO = this.updateMatch(match);

            if(match.getGameState().getPlayerWin().equals(match.getGameState().getPlayerOne().getId())){
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.FINISHED_GAME.toString(),
                        OperationStatus.OK,
                        "Partida finalizada",
                        matchDTO);

                communication.sendToUser(match.getPlayer1().getUserSession(), response);
                communication.sendToUser(match.getPlayer2().getUserSession(), response);
                matchManagementService.unregisterMatch(match.getMatchId());
                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer1().getNickname());
                return;
            }else if(match.getGameState().getPlayerWin().equals("DRAW")){
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.FINISHED_DRAW.toString(),
                        OperationStatus.OK,
                        "Partida finalizada",
                        matchDTO);

                communication.sendToUser(match.getPlayer1().getUserSession(), response);
                communication.sendToUser(match.getPlayer2().getUserSession(), response);
                matchManagementService.unregisterMatch(match.getMatchId());
                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), "DRAW");
                return;
            }
            else if(match.getGameState().getPlayerWin().equals(match.getGameState().getPlayerTwo().getId())){
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.FINISHED_GAME.toString(),
                        OperationStatus.OK,
                        "Partida finalizada",
                        matchDTO);

                communication.sendToUser(match.getPlayer1().getUserSession(), response);
                communication.sendToUser(match.getPlayer2().getUserSession(), response);
                matchManagementService.unregisterMatch(match.getMatchId());
                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer2().getNickname());
                return;
            }

            OperationResponseDTO response = new OperationResponseDTO(
                    OperationType.UPDATE_GAME.toString(),
                    OperationStatus.OK,
                    "Partida atualizada",
                    matchDTO);

            communication.sendToUser(match.getPlayer1().getUserSession(), response);
            communication.sendToUser(match.getPlayer2().getUserSession(), response);
        } else {
            match.playCard(userID);
            MatchFoundResponseDTO matchDTO = this.updateMatch(match);

            if(match.getGameState().getPlayerWin().equals(match.getGameState().getPlayerOne().getId())){
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.FINISHED_GAME.toString(),
                        OperationStatus.OK,
                        "Partida finalizada",
                        matchDTO);

                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer1().getNickname());

                Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

                newPayload.put("response", response);

                OperationRequestDTO newOperation = new OperationRequestDTO(
                        operation.getOperationType(),
                        newPayload
                );

                if (!match.getManagerNodeId().equals(currentNodeId) ) {
                    communication.sendToUser(match.getPlayer2().getUserSession(), response);
                    redirectService.sendOperationRequestToNode(
                            match.getPlayer1().getHostAddress(),
                            "UpdateGame",
                            newOperation,
                            HttpMethod.POST
                    );
                }else {
                    communication.sendToUser(match.getPlayer1().getUserSession(), response);
                    redirectService.sendOperationRequestToNode(
                            match.getPlayer2().getHostAddress(),
                            "UpdateGame",
                            newOperation,
                            HttpMethod.POST
                    );
                }
                matchManagementService.unregisterMatch(match.getMatchId());
                return;
            }

            else if(match.getGameState().getPlayerWin().equals("DRAW")){
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.FINISHED_DRAW.toString(),
                        OperationStatus.OK,
                        "Partida finalizada",
                        matchDTO);

                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), "DRAW");

                Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

                newPayload.put("response", response);

                OperationRequestDTO newOperation = new OperationRequestDTO(
                        operation.getOperationType(),
                        newPayload
                );

                if (!match.getManagerNodeId().equals(currentNodeId) ) {
                    communication.sendToUser(match.getPlayer2().getUserSession(), response);
                    redirectService.sendOperationRequestToNode(
                            match.getPlayer1().getHostAddress(),
                            "UpdateGame",
                            newOperation,
                            HttpMethod.POST
                    );
                } else {
                    communication.sendToUser(match.getPlayer1().getUserSession(), response);
                    redirectService.sendOperationRequestToNode(
                            match.getPlayer2().getHostAddress(),
                            "UpdateGame",
                            newOperation,
                            HttpMethod.POST
                    );
                }

                matchManagementService.unregisterMatch(match.getMatchId());
                return;
            }


            else if(match.getGameState().getPlayerWin().equals(match.getGameState().getPlayerTwo().getId())){
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.FINISHED_GAME.toString(),
                        OperationStatus.OK,
                        "Partida finalizada",
                        matchDTO);

                truffleApiUser.registryMatch(match.getPlayer1().getNickname(), match.getPlayer2().getNickname(), match.getPlayer2().getNickname());

                Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

                newPayload.put("response", response);

                OperationRequestDTO newOperation = new OperationRequestDTO(
                        operation.getOperationType(),
                        newPayload
                );

                if (!match.getManagerNodeId().equals(currentNodeId) ) {
                    communication.sendToUser(match.getPlayer2().getUserSession(), response);
                    redirectService.sendOperationRequestToNode(
                            match.getPlayer1().getHostAddress(),
                            "UpdateGame",
                            newOperation,
                            HttpMethod.POST
                    );
                } else {
                    communication.sendToUser(match.getPlayer1().getUserSession(), response);
                    redirectService.sendOperationRequestToNode(
                            match.getPlayer2().getHostAddress(),
                            "UpdateGame",
                            newOperation,
                            HttpMethod.POST
                    );
                }

                matchManagementService.unregisterMatch(match.getMatchId());
                return;
            }

            OperationResponseDTO response = new OperationResponseDTO(
                    OperationType.UPDATE_GAME.toString(),
                    OperationStatus.OK,
                    "Partida atualizada",
                    matchDTO);

            // ------------Envia response para o outro nó--------------
            Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

            newPayload.put("response", response);

            OperationRequestDTO newOperation = new OperationRequestDTO(
                    operation.getOperationType(),
                    newPayload
            );

            if (!match.getManagerNodeId().equals(currentNodeId) ) {
                System.out.println("Jogo Distribuído, estou no servidor não gerenciador da partida " + userSession + " apertou play e chegou aqui");
                communication.sendToUser(match.getPlayer2().getUserSession(), response);
                redirectService.sendOperationRequestToNode(
                        match.getManagerNodeId(),
                        "UpdateGame",
                        newOperation,
                        HttpMethod.POST
                );
            } else {
                System.out.println("Jogo Distribuído, estou no servidor gerenciador da partida " + userSession + " apertou play e chegou aqui");
                communication.sendToUser(match.getPlayer1().getUserSession(), response);
                redirectService.sendOperationRequestToNode(
                        match.getPlayer2().getHostAddress(),
                        "UpdateGame",
                        newOperation,
                        HttpMethod.POST
                );
            }
        }
    }

    private MatchFoundResponseDTO updateMatch(Match match) {
        List<CardDTO> player1 = new ArrayList<>();
        for (Card card : match.getGameState().getPlayerOne().getCards()) {
            player1.add(new CardDTO(card));
        }

        List<CardDTO> player2 = new ArrayList<>();
        for (Card card : match.getGameState().getPlayerTwo().getCards()) {
            player2.add(new CardDTO(card));
        }

        GameStateDTO gameStateDTO = new GameStateDTO(match.getGameState(), player1, player2);
        matchManagementService.updateMatch(match);
        return new MatchFoundResponseDTO(
                match.getMatchId(),
                match.getManagerNodeId(),
                gameStateDTO,
                match.getPlayer1(),
                match.getPlayer2(),
                match.isLocalMatch()
        );
    }

    public void handleActionActivateCard(OperationRequestDTO operation, String userSession) {
        String userID = (String) operation.getPayload().get("userID");
        String cardID = (String) operation.getPayload().get("cardID");

        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();
        Match match = this.getMatch(operation);

        if(match == null){
            this.sendMessageNotFoundMath(userSession, operation);
            return;
        }

        if(match.getIslocalMatch()) {
            if(match.getGameState().getPlayerOne().getId().equals(userID)){
                match.getGameState().getPlayerOne().setActivationCard(cardID);
            }else{
                match.getGameState().getPlayerTwo().setActivationCard(cardID);
            }

            MatchFoundResponseDTO matchDTO = this.updateMatch(match);

            OperationResponseDTO response = new OperationResponseDTO(
                OperationType.UPDATE_GAME.toString(),
                OperationStatus.OK,
            "Partida atualizada",
                matchDTO);

            communication.sendToUser(match.getPlayer1().getUserSession(), response);
            communication.sendToUser(match.getPlayer2().getUserSession(), response);
        } else {
            // PS dica doq acho que tem que ser feito; Processa aqui a ação e dps envia ao outro nó com o código abaixo
            if(match.getGameState().getPlayerOne().getId().equals(userID)){
                match.getGameState().getPlayerOne().setActivationCard(cardID);
            }else{
                match.getGameState().getPlayerTwo().setActivationCard(cardID);
            }

            MatchFoundResponseDTO matchDTO = this.updateMatch(match);

            OperationResponseDTO response = new OperationResponseDTO(
                    OperationType.UPDATE_GAME.toString(),
                    OperationStatus.OK,
                    "Partida atualizada",
                    matchDTO);

            // ---------------------Envio para o outro nó----------------------------
            Map<String, Object> newPayload = new HashMap<>(operation.getPayload());

            newPayload.put("response", response);

            OperationRequestDTO newOperation = new OperationRequestDTO(
                    operation.getOperationType(),
                    newPayload
            );

            if (!match.getManagerNodeId().equals(currentNodeId) ) {
                System.out.println("Jogo Distribuído, estou no servidor não gerenciador da partida " + userSession + " ativei carta e chegou aqui");
                communication.sendToUser(match.getPlayer2().getUserSession(), response);
                redirectService.sendOperationRequestToNode(
                        match.getManagerNodeId(),
                        "UpdateGameActiveCard",
                        newOperation,
                        HttpMethod.POST
                );
            } else {
                System.out.println("Jogo Distribuído, estou no servidor gerenciador da partida " + userSession + " ativei carta e chegou aqui");
                communication.sendToUser(match.getPlayer1().getUserSession(), response);
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

    private Match getMatch(OperationRequestDTO operation){
        String matchID = (String) operation.getPayload().get("matchID");
        return matchManagementService.getMatchState(matchID);
    }

    private void sendMessageNotFoundMath(String userSession, OperationRequestDTO operation){
        OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(),OperationStatus.ERROR, "Partida não encontrada!", null);
        communication.sendToUser(userSession, response);
    }
}
