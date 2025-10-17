package com.avatar.avatar_online.raft.model;

public class AppendEntriesResponse {
    private boolean success;
    private boolean logMismatch;

    public AppendEntriesResponse() {}
    public AppendEntriesResponse(boolean success, boolean logMismatch) {
        this.success = success;
        this.logMismatch = logMismatch;
    }

    // Getters e Setters
    public boolean isSuccess() { return success; }
    public boolean isLogMismatch() { return logMismatch; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setLogMismatch(boolean logMismatch) { this.logMismatch = logMismatch; }

    public static AppendEntriesResponse success() { return new AppendEntriesResponse(true, false); }
    public static AppendEntriesResponse logMismatch() { return new AppendEntriesResponse(false, true); }
    public static AppendEntriesResponse failure() { return new AppendEntriesResponse(false, false); }
}
