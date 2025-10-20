package com.avatar.avatar_online.publisher_subscriber.model;

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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public OperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(OperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
