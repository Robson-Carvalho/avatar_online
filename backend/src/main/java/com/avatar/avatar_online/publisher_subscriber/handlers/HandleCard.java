package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.*;
import com.avatar.avatar_online.Truffle_Comunication.TruffleApiUser;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.GetCardsResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.ProposalDTO;
import com.avatar.avatar_online.publisher_subscriber.model.*;
import com.avatar.avatar_online.publisher_subscriber.service.Communication;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.service.CardService;
import com.avatar.avatar_online.service.UserService;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class HandleCard {
    private final CardService cardService;
    private final OnlineUsers onlineUsers;
    private final Communication communication;
    private final HazelcastInstance hazelcast;
    private final RedirectService redirectService;
    private final TruffleApiUser truffleApiUser;
    private final UserService userService;

    @Autowired
    public HandleCard(CardService cardService, OnlineUsers onlineUsers, Communication communication,
                      @Qualifier("hazelcastInstance") HazelcastInstance hazelcast, RedirectService redirectService,
                      TruffleApiUser truffleApiUser, UserService userService) {
        this.cardService = cardService;
        this.onlineUsers = onlineUsers;
        this.communication = communication;
        this.hazelcast = hazelcast;
        this.redirectService = redirectService;
        this.truffleApiUser = truffleApiUser;
        this.userService = userService;
    }

    public OperationResponseDTO handleGetCards(OperationRequestDTO operation){
        String userID = (String) operation.getPayload().get("userID");
        Optional<User> userOptional = userService.findById(UUID.fromString(userID));

        if(userOptional.isEmpty()){
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: ao buscar usuário", null);
        }

        try {
            List<CardDTO> cards = cardService.findByUserId(UUID.fromString(userID));
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Cartas do user: "+userID, cards);
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: "+e.getMessage(), null);
        }
    }

    public OperationResponseDTO handleGetCardsByPlayerId(OperationRequestDTO operation){
        String userID = (String) operation.getPayload().get("userID");

        try {
            List<CardDTO> cards = cardService.findByUserIdWithoutDeck(UUID.fromString(userID));
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Cartas do user: "+userID, cards);
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: "+e.getMessage(), null);
        }
    }

    public void handleProposalExchangeCard(OperationRequestDTO operation, String sessionId){
        String player1ID = (String) operation.getPayload().get("player1ID");
        String card1ID = (String) operation.getPayload().get("card1ID");

        String player2ID = (String) operation.getPayload().get("player2ID");
        String card2ID = (String) operation.getPayload().get("card2ID");

        Optional<OnlineUserInfo> op = onlineUsers.getUserInfoByUserId(player2ID);

        if(op.isEmpty()){
            System.out.println("Player "+player2ID+" nao encontrado");
            OperationResponseDTO response =  new OperationResponseDTO(OperationType.PROPOSAL_EXCHANGE_CARD_SENDER.toString(), OperationStatus.WARNING, "Player não está online!", null);
            communication.sendToUser(sessionId, response);
            return;
        }

        OnlineUserInfo player2 = op.get();
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();

        try {
            CardDTO card1 = cardService.findById(UUID.fromString(card1ID));
            CardDTO card2 = cardService.findById(UUID.fromString(card2ID));

            if (card1 == null || card2 == null) {
                OperationResponseDTO responseUser = new OperationResponseDTO(OperationType.PROPOSAL_EXCHANGE_CARD_SENDER.toString(), OperationStatus.ERROR, "Card ID não existe!", null);
                communication.sendToUser(sessionId, responseUser);
                return;
            }

            ProposalDTO proposalDTO = new ProposalDTO(
                    card1.getId().toString(),
                    card1.getName(),
                    card1.getElement().toString(),
                    card1.getPhase().toString(),
                    card1.getAttack(),
                    card1.getLife(),
                    card1.getDefense(),
                    card1.getRarity().toString(),

                    card2.getId().toString(),
                    card2.getName(),
                    card2.getElement().toString(),
                    card2.getPhase().toString(),
                    card2.getAttack(),
                    card2.getLife(),
                    card2.getDefense(),
                    card2.getRarity().toString(),

                    // IDs dos jogadores
                    player1ID,
                    player2ID
            );

            if (Objects.equals(currentNodeId, player2.getHost())) {
                OperationResponseDTO responseUser = new OperationResponseDTO(OperationType.PROPOSAL_EXCHANGE_CARD_SENDER.toString(), OperationStatus.OK, "Proposta enviada!", null);
                OperationResponseDTO responsePlayer2 = new OperationResponseDTO(OperationType.PROPOSAL_EXCHANGE_CARD_RECEIVER.toString(), OperationStatus.OK, "Você recebeu uma proposta de troca", proposalDTO);

                communication.sendToUser(sessionId, responseUser);
                communication.sendToUser(player2.getSessionId(), responsePlayer2);
            } else {
                OperationResponseDTO responseUser = new OperationResponseDTO(OperationType.PROPOSAL_EXCHANGE_CARD_SENDER.toString(), OperationStatus.OK, "Proposta enviada!", null);
                OperationResponseDTO responsePlayer2 = new OperationResponseDTO(OperationType.PROPOSAL_EXCHANGE_CARD_RECEIVER.toString(), OperationStatus.OK, "Você recebeu uma proposta de troca", proposalDTO);

                communication.sendToUser(sessionId, responseUser);
                redirectService.sendOperationResponseCardToNode(player2.getHost(),"proposal/"+player2.getSessionId(), responsePlayer2, HttpMethod.POST);
            }
        }catch (Exception e){
            OperationResponseDTO response = new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro inesperado: " + e.getMessage(), null);
            communication.sendToUser(sessionId, response);
        }
    }

    public void sendProposal(OperationResponseDTO response, String sessionId){
        communication.sendToUser(sessionId, response);
    }

    public OperationResponseDTO handleExchangeCard(OperationRequestDTO operation){
        String player1ID = (String) operation.getPayload().get("player1ID");
        String card1ID = (String) operation.getPayload().get("card1ID");

        String player2ID = (String) operation.getPayload().get("player2ID");
        String card2ID = (String) operation.getPayload().get("card2ID");

        try{
            TradeCardDTO tradeCardDTO = new TradeCardDTO(player1ID,player2ID, card1ID, card2ID);
            ResponseEntity<?> response =  cardService.tradeCard(tradeCardDTO);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK,"Troca realziada com sucesso!", null);
            }else{
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.WARNING,"Algo aconteceu durante a troca!", null);
            }
        }catch (Exception e){
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro inesperado: " + e.getMessage(), null);
        }
    }

    public OperationResponseDTO handleOpenPackage(OperationRequestDTO operation) {
        String userID = (String) operation.getPayload().get("userID");

        try {
            ResponseEntity<?> response = cardService.generatePack(new PackDTO(userID));

            if (response.getStatusCode().is2xxSuccessful()) {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Cartas selecionadas", response.getBody());
            } else{
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Não foi possível abrir pacote!", null);
            }
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro inesperado: " + e.getMessage(), null
            );
        }
    }
}
