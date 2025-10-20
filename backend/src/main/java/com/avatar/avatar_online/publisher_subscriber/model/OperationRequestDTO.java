package com.avatar.avatar_online.publisher_subscriber.model;

import java.util.Map;

public class OperationRequestDTO {
    private String operationType;
    private Map<String, Object> payload; // conteúdo variável por operação

    public OperationRequestDTO() {}

    public OperationRequestDTO(String operationType, Map<String, Object> payload) {
        this.operationType = operationType;
        this.payload = payload;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}

