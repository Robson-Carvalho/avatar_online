package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.raft.logs.UserSignUpCommand;
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

    @GetMapping("/export/users")
    public List<DatabaseSyncService.UserExport> exportUsers() {
        System.out.println("🌍 Requisição de exportação de usuários recebida. Exportando dados...");
        return databaseSyncService.performLeaderSync();
    }
}
