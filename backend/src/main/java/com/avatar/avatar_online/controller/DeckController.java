package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.DTOs.DeckDTO;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.service.DeckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/deck")
public class DeckController {
    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping()
    public ResponseEntity<?> getAllDecks() {
        try{
            List<Deck> deck = deckService.findAll();
            return ResponseEntity.ok(deck);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"Falha ao buscar usuários: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping()
    public ResponseEntity<?> updateDeck(@RequestBody DeckDTO deckDTO) {
        try{
            return deckService.updateDeck(deckDTO);
        } catch (Exception e){
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao processar requisição: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getDeckByUser(@PathVariable String id) {
        try{
            Optional<Deck> deck = deckService.findByUserId(id);

            if(deck.isPresent()) {
                return ResponseEntity.ok(deck.get());
            }

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"Falha ao buscar usuários: " + e.getMessage() + "\"}");
        }
    }
}
