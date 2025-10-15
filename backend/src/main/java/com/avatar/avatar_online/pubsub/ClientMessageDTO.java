package com.avatar.avatar_online.pubsub;

public class ClientMessageDTO {
    private String clientID;
    private String commandType;
    private String payload;

    public String getClientID() { return clientID; }
    public void setClientID(String clientID) { this.clientID = clientID; }
    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}