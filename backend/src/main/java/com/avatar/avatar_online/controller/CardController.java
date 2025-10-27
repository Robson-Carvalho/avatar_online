package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.DTOs.PackDTO;
import com.avatar.avatar_online.DTOs.TradeCardDTO;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.publisher_subscriber.handlers.HandleCard;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private HandleCard handleCard;
    private final CardService cardService;

    public CardController(CardService cardService,  HandleCard handleCard) {
        this.cardService = cardService;
        this.handleCard = handleCard;
    }

    @PostMapping("/pack")
    public ResponseEntity<?> getPack(@RequestBody PackDTO packDTO) {
        try {
            return cardService.generatePack(packDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao processar requisição: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/proposal/{sessionId}")
    public ResponseEntity<?> proposal( @PathVariable String sessionId, @RequestBody OperationResponseDTO response) {
        try {
            handleCard.sendProposal(response, sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao processar requisição: " + e.getMessage() + "\"}");
        }
    }
    
    @PostMapping("/trade")
    public ResponseEntity<?> tradeCard(@RequestBody TradeCardDTO tradeCardDTO) {
        try {
            return cardService.tradeCard(tradeCardDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao processar requisição: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCards() {
        try {
            List<Card> cards = cardService.findAll();
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Falha ao buscar cartas: " + e.getMessage() + "\"}");
        }
    }
}
