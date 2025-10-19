package com.avatar.publisher_subscriber.handlers;

import com.avatar.DTOs.PackDTO;
import com.avatar.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.publisher_subscriber.model.OperationStatus;
import com.avatar.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class HandleCard {
    private final CardService cardService;

    @Autowired
    public HandleCard(CardService cardService) {
        this.cardService = cardService;
    }

    public OperationResponseDTO handleOpenPackage(OperationRequestDTO operation) {
        String userID = (String) operation.getPayload().get("userID");

        try {
            ResponseEntity<?> response = cardService.generatePack(new PackDTO(userID));

            if (response.getStatusCode().is2xxSuccessful()) {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Cartas selecionadas", response.getBody());
            } else{
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Não foi possível abrir pacote!", null);
            }
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro inesperado: " + e.getMessage(), null
            );
        }
    }
}
