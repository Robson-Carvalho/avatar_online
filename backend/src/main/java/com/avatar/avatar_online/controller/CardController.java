package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
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
