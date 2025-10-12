package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.model.CardExport;
import com.avatar.avatar_online.raft.model.DeckExport;
import com.avatar.avatar_online.raft.model.UserExport;
import com.avatar.avatar_online.raft.service.DatabaseSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    private final DatabaseSyncService databaseSyncService;

    public SyncController(DatabaseSyncService databaseSyncService) {
        this.databaseSyncService = databaseSyncService;
    }

    @PostMapping("/apply-commit/users")
    public ResponseEntity<?> applyUserCommit (@RequestBody UserSignUpCommand command){
        try {
            databaseSyncService.applyUserSignUpCommand(command);
            return ResponseEntity.ok().body("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erro ao Commitar usuários: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/apply-commit/pack")
    public ResponseEntity<?> applyPackCommit (@RequestBody List<Card> cards){
        try {
            databaseSyncService.applyPackCommit(cards);
            return ResponseEntity.ok().body("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erro ao Commitar pack: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/apply-commit/deck")
    public ResponseEntity<?> applyDeckCommit (@RequestBody SetDeckCommmand command){
        try {
            databaseSyncService.applyDeckUpdateCommand(command);
            return ResponseEntity.ok().body("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erro ao Commitar atualização de deck: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/export/users")
    public List<UserExport> exportUsers() {
        System.out.println("🌍 Requisição de exportação de usuários recebida. Exportando dados...");
        return databaseSyncService.performLeaderSync();
    }

    @GetMapping("/export/cards")
    public List<CardExport> exportCards() {
        System.out.println("🌍 Requisição de exportação de cartas recebida. Exportando dados...");
        return databaseSyncService.performLeaderCardSync();
    }

    @GetMapping("/export/decks")
    public List<DeckExport> exportDecks() {
        System.out.println("🌍 Requisição de exportação de decks recebida. Exportando dados...");
        return databaseSyncService.performLeaderDeckSync();
    }
}
