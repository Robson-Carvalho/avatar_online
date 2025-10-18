package com.avatar.publisher_subscriber.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class OperationRequestDTO {
    private String operationType;
    private Map<String, Object> payload; // conteúdo variável por operação

    public OperationRequestDTO(OperationType String, Map<String, Object> payload) {
        this.operationType = operationType;
        this.payload = payload;
    }
}
