package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.DeckDTO;
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

import java.util.ArrayList;
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
            Optional<Deck> deckOpt = deckService.findByUserId(userID);

            if (deckOpt.isPresent()) {
                Deck deck = deckOpt.get();

                List<CardDTO> allCards = cardService.findByUserId(UUID.fromString(userID));

                List<UUID> deckCardIds = new ArrayList<>();
                if (deck.getCard1() != null) deckCardIds.add(deck.getCard1());
                if (deck.getCard2() != null) deckCardIds.add(deck.getCard2());
                if (deck.getCard3() != null) deckCardIds.add(deck.getCard3());
                if (deck.getCard4() != null) deckCardIds.add(deck.getCard4());
                if (deck.getCard5() != null) deckCardIds.add(deck.getCard5());

                List<CardDTO> cardsInDeck = allCards.stream()
                        .filter(c -> deckCardIds.contains(c.getId()))
                        .toList();

                GetDeckResponseDTO getDeckResponseDTO = new GetDeckResponseDTO(cardsInDeck, allCards);

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
        String cardId1Str = (String) operation.getPayload().get("cardId1");
        String cardId2Str = (String) operation.getPayload().get("cardId2");
        String cardId3Str = (String) operation.getPayload().get("cardId3");
        String cardId4Str = (String) operation.getPayload().get("cardId4");
        String cardId5Str = (String) operation.getPayload().get("cardId5");

        UUID cardId1 = cardId1Str != null ? UUID.fromString(cardId1Str) : null;
        UUID cardId2 = cardId2Str != null ? UUID.fromString(cardId2Str) : null;
        UUID cardId3 = cardId3Str != null ? UUID.fromString(cardId3Str) : null;
        UUID cardId4 = cardId4Str != null ? UUID.fromString(cardId4Str) : null;
        UUID cardId5 = cardId5Str != null ? UUID.fromString(cardId5Str) : null;

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