package com.avatar.avatar_online.DTOs;


public class HistoryResponseDTO {
    private String status;
    private String command;
    private HistoryDataWrapperDTO data;

    public HistoryResponseDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public HistoryDataWrapperDTO getData() { return data; }
    public void setData(HistoryDataWrapperDTO data) { this.data = data; }
}
