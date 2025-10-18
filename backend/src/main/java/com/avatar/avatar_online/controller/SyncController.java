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
    private final LogStore logStore;

    public SyncController(LogStore logStore) {
        this.logStore = logStore;
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
}
