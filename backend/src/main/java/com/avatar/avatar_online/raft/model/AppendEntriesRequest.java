package com.avatar.avatar_online.raft.model;

import java.util.List;

public class AppendEntriesRequest {
        private long leaderTerm;
        private long leaderCommitIndex;
        private long prevLogIndex;
        private long prevLogTerm;
        private List<LogEntry> entries;

        public AppendEntriesRequest() {}

        public AppendEntriesRequest(long leaderTerm, long leaderCommitIndex, long prevLogIndex, long prevLogTerm, List<LogEntry> entries) {
            this.leaderTerm = leaderTerm;
            this.leaderCommitIndex = leaderCommitIndex;
            this.prevLogIndex = prevLogIndex;
            this.prevLogTerm = prevLogTerm;
            this.entries = entries;
        }

    public long getLeaderTerm() {
        return leaderTerm;
    }

    public void setLeaderTerm(long leaderTerm) {
        this.leaderTerm = leaderTerm;
    }

    public long getLeaderCommitIndex() {
        return leaderCommitIndex;
    }

    public void setLeaderCommitIndex(long leaderCommitIndex) {
        this.leaderCommitIndex = leaderCommitIndex;
    }

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public long getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(long prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }
}
