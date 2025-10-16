package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.model.CardExport;
import com.avatar.avatar_online.raft.model.DeckExport;
import com.avatar.avatar_online.raft.model.LogEntry;
import com.avatar.avatar_online.raft.model.UserExport;
import com.avatar.avatar_online.raft.service.DatabaseSyncService;
import com.avatar.avatar_online.raft.service.LogStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    private final DatabaseSyncService databaseSyncService;
    private final LogStore logStore;

    public SyncController(DatabaseSyncService databaseSyncService, LogStore logStore) {
        this.databaseSyncService = databaseSyncService;
        this.logStore = logStore;
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

    @PostMapping("/append-entries")
    public ResponseEntity<String> appendEntries(@RequestBody LogEntry entry) {
        try {

            logStore.append(entry);

            return ResponseEntity.ok().body("{\"success\": true, \"index\": " + entry.getIndex() + "}");

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Falha de Consistência (AppendEntries): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"success\": false, \"error\": \"Inconsistência de Log: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("❌ Erro ao persistir LogEntry: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\": false, \"error\": \"Falha interna: " + e.getMessage() + "\"}");
        }
    }
}
