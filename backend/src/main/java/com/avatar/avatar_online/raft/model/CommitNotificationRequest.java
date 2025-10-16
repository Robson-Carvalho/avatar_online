package com.avatar.avatar_online.raft.model;


public class CommitNotificationRequest {
    private long commitIndex;

    public CommitNotificationRequest() {
    }

    public CommitNotificationRequest(long newIndex) {
        this.commitIndex = newIndex;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }
}