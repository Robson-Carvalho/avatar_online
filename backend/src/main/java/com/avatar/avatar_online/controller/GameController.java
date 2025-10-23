package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.publisher_subscriber.handlers.HandleGame;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/notify/UpdateGame")
    public ResponseEntity<?> notifyUpdateGame(@RequestBody OperationRequestDTO orD){
        try{
            Map<String, Object> payload = orD.getPayload();
            String userSession = (String) payload.get("userSession");

            Map<String, Object> cleanedPayload = new HashMap<>(payload);
            cleanedPayload.remove("userSession");

            orD.setPayload(cleanedPayload);

            handleGame.ProcessPlayCardFromOtherNode(orD, userSession);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao enviar notificação de PlayCard: " + e.getMessage() + "\"}");
        }
    }
}
