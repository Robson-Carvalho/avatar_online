package com.avatar.avatar_online.pubsub;

import java.time.Instant;

public class ServerEventDTO {
    private String recipientId;
    private String eventType;
    private final Instant timestamp = Instant.now();
    private Object data;

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Instant getTimestamp() { return timestamp; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}