package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.DeckDTO;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.GetDeckResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationStatus;
import com.avatar.avatar_online.service.CardService;
import com.avatar.avatar_online.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class HandleDeck {
    private final DeckService deckService;
    private final CardService cardService;

    @Autowired
    public HandleDeck(DeckService deckService, CardService cardService) {
        this.deckService = deckService;
        this.cardService = cardService;
    }

    public OperationResponseDTO handleGetDeck(OperationRequestDTO operation){
        String userID = (String) operation.getPayload().get("userID");

        try {
            Optional<Deck> deck = deckService.findByUserId(userID);

            if (deck.isPresent()) {
                List<CardDTO> cards = cardService.findByUserId(UUID.fromString(userID));
                GetDeckResponseDTO  getDeckResponseDTO = new GetDeckResponseDTO(deck, cards);

                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Deck do user: "+userID, getDeckResponseDTO);
            } else{
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Nenhum deck encontrado!", null);
            }
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: "+e.getMessage(), null);
        }
    }

    public OperationResponseDTO handleUpdateDeck(OperationRequestDTO operation){
        UUID userID = UUID.fromString((String) operation.getPayload().get("userID"));
        UUID cardId1 = UUID.fromString((String) operation.getPayload().get("cardId1"));
        UUID cardId2 = UUID.fromString((String) operation.getPayload().get("cardId2"));
        UUID cardId3 = UUID.fromString((String) operation.getPayload().get("cardId3"));
        UUID cardId4 = UUID.fromString((String) operation.getPayload().get("cardId4"));
        UUID cardId5 = UUID.fromString((String) operation.getPayload().get("cardId5"));

        try {
            DeckDTO deckToUpdate = new DeckDTO(userID, cardId1, cardId2, cardId3, cardId4, cardId5);

            ResponseEntity<?> response = deckService.updateDeck(deckToUpdate);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Deck atualizado com sucesso!", null);
            } else{
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Não foi possível salvar o Deck!", null);
            }
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: "+e.getMessage(), null);
        }
    }
}