package com.avatar.avatar_online.pubsub;

import java.time.Instant;

// Representa o que a Lógica do Jogo publica e o Gateway repassa
public class ServerEventDTO {
    private String recipientId;
    private String eventType;
    private Instant timestamp = Instant.now();
    private Object data;

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}