package com.avatar.avatar_online.raft.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class LogEntry {

    private long term;
    private long index;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private Object command;
    private boolean commited;

    public LogEntry(long term, long index, Object command, boolean commited) {
        this.term = term;
        this.index = index;
        this.command = command;
        this.commited = commited;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public Object getCommand() {
        return command;
    }

    public void setCommand(Object command) {
        this.command = command;
    }

    public boolean isCommited() {
        return commited;
    }

    public void setCommited(boolean commited) {
        this.commited = commited;
    }
}
