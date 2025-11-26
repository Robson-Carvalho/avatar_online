package com.avatar.avatar_online.DTOs;

public class TimelineItemDTO {
    private String type;
    private long timestamp;
    private String transaction;
    private TimelineDataDTO data;

    public TimelineItemDTO() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public TimelineDataDTO getData() {
        return data;
    }

    public void setData(TimelineDataDTO data) {
        this.data = data;
    }
}
