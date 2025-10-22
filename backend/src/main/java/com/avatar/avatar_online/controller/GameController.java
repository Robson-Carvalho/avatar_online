package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.publisher_subscriber.handlers.HandleGame;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final HandleGame handleGame;

    @Autowired
    public GameController(HandleGame handleGame) {
        this.handleGame = handleGame;
    }

    @PostMapping("/notify/GameFound")
    public ResponseEntity<?> notifyGameFound(@RequestBody OperationResponseDTO orD){
        try{
            handleGame.handleNotifyGameFound(orD);
            return ResponseEntity.ok().build();
        } catch(Exception e){
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao enviar notificação de GameFound: " + e.getMessage() + "\"}");
        }
    }
}
