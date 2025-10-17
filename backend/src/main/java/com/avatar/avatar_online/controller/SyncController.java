package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
import com.avatar.avatar_online.raft.model.*;
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
    public ResponseEntity<AppendEntriesResponse> appendEntries(@RequestBody AppendEntriesRequest request) {
        long prevLogIndex = request.getPrevLogIndex();

        if (prevLogIndex > 0) {
            long requiredTerm = logStore.getTermOfIndex(prevLogIndex);

            if (requiredTerm != request.getPrevLogTerm()) {
                System.err.println("❌ Log Inconsistente no índice " + prevLogIndex +
                        ". Esperado Termo: " + requiredTerm +
                        ", Recebido Termo: " + request.getPrevLogTerm());

                return ResponseEntity.ok(AppendEntriesResponse.logMismatch());
            }
        }

        if (request.getEntries() != null && !request.getEntries().isEmpty()) {

            int index = 0;
            for (LogEntry entry : request.getEntries()) {
                long newEntryIndex = entry.getIndex();

                if (logStore.getLastIndex() >= newEntryIndex) {
                    long currentTermAtIndex = logStore.getTermOfIndex(newEntryIndex);

                    if (currentTermAtIndex != entry.getTerm()) {
                        System.out.println("⚠️ Conflito no Log. Truncando a partir do índice: " + newEntryIndex);
                        logStore.truncateLog(newEntryIndex);
                        break;
                    }
                }
                index++;
            }

            for (LogEntry entry : request.getEntries()) {
                if (logStore.getLastIndex() >= entry.getIndex() && logStore.getTermOfIndex(entry.getIndex()) == entry.getTerm()) {
                    continue;
                }
                logStore.append(entry);
            }
        }

        // 4. Avançar o Commit Index (Regra R-AE.5)
        long leaderCommitIndex = request.getLeaderCommitIndex();
        if (leaderCommitIndex > logStore.getLastCommitIndex()) {
            long newCommitIndex = Math.min(leaderCommitIndex, logStore.getLastIndex());
            logStore.markCommitted(newCommitIndex);
        }

        return ResponseEntity.ok(AppendEntriesResponse.success());
    }

    @PostMapping("/commit-notification")
    public ResponseEntity<String> notifyCommit(@RequestBody CommitNotificationRequest request) {
        long leaderCommitIndex = request.getCommitIndex();
        long localCommitIndex = logStore.getLastCommitIndex();

        if (leaderCommitIndex > localCommitIndex) {
            long newCommitIndex = Math.min(leaderCommitIndex, logStore.getLastIndex());
            logStore.markCommitted(newCommitIndex);
        }

        return ResponseEntity.ok().body("OK");
    }
}
