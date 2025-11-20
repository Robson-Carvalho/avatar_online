package com.avatar.avatar_online.DTOs;

public class TruffleApiWrapper<T> {
    private String status;
    private String command;
    private T data;

    public TruffleApiWrapper() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
