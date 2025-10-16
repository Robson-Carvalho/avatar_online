package com.avatar.avatar_online.web;

public class ServerResponse {
    private String content;
    private String replyToSessionId;

    public ServerResponse(String content, String replyToSessionId) {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReplyToSessionId() {
        return replyToSessionId;
    }

    public void setReplyToSessionId(String replyToSessionId) {
        this.replyToSessionId = replyToSessionId;
    }
}