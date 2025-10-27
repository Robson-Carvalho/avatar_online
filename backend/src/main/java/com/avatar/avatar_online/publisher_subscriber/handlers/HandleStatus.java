package com.avatar.avatar_online.publisher_subscriber.handlers;


import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationStatus;
import com.avatar.avatar_online.publisher_subscriber.model.OperationType;
import org.springframework.stereotype.Component;

@Component
public class HandleStatus {

    public OperationResponseDTO handlePing(OperationRequestDTO operation){
        try {
            return new OperationResponseDTO(OperationType.PONG.toString(), OperationStatus.OK,"Pong", false);
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro inesperado: " + e.getMessage(), null
            );
        }
    }
}
