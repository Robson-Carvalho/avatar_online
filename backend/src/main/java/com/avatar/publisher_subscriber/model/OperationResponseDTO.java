package com.avatar.publisher_subscriber.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationResponseDTO {
    private String operationType;
    private OperationStatus operationStatus;
    private String message;
    private Object data;

    public OperationResponseDTO() {}

    public OperationResponseDTO(String operationType,OperationStatus operationStatus, String message, Object data) {
        this.operationType = operationType;
        this.operationStatus = operationStatus;
        this.message = message;
        this.data = data;
    }
}
