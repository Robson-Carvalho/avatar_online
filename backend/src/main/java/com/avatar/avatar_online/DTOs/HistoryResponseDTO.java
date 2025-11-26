package com.avatar.avatar_online.DTOs;


public class HistoryResponseDTO {
    private String status;
    private String command;
    private HistoryDataDTO data;

    public HistoryResponseDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public HistoryDataDTO getData() { return data; }
    public void setData(HistoryDataDTO data) { this.data = data; }
}
