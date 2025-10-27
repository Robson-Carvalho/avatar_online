package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.PackDTO;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationStatus;
import com.avatar.avatar_online.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class HandleCard {
    private final CardService cardService;

    @Autowired
    public HandleCard(CardService cardService) {
        this.cardService = cardService;
    }

    public OperationResponseDTO handleGetCards(OperationRequestDTO operation){
        String userID = (String) operation.getPayload().get("userID");

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

    public OperationResponseDTO handleProposalExchangeCard(OperationRequestDTO operation){
        return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: ", null);

    }

    public OperationResponseDTO handleExchangeCard(OperationRequestDTO operation){
        return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: ",null);

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
