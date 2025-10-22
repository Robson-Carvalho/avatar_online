package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class HandleGame {

    public OperationResponseDTO handleJoinInQueue(OperationRequestDTO operation){
        String userID = (String) operation.getPayload().get("userID");



        return new OperationResponseDTO() ;
    }

    public OperationResponseDTO handlePlayCard(OperationRequestDTO operation){
        return new OperationResponseDTO() ;
    }

    public OperationResponseDTO handleActivateCard(OperationRequestDTO operation){
        return new OperationResponseDTO() ;
    }
}
