package com.avatar.controller;

import com.avatar.DTOs.PackDTO;
import com.avatar.models.Card;
import com.avatar.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
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
