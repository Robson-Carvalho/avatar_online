package com.avatar.avatar_online.raft.logs;

import java.io.Serializable;
import java.util.UUID;

public class UserSignUpCommand implements Serializable {
    private final UUID commandId;

    private final String operationType;

    private final UUID deckId;

    private final UUID playerId;

    private final String name;

    private final String email;

    private final String nickname;

    private final String password;

    private final String privateKey;

    private final String address;

    public  UserSignUpCommand(UUID commandId, String operationType, UUID deckId, UUID playerId, String name,
                              String email, String nickname, String password, String privateKey, String address) {
        this.commandId = commandId;
        this.operationType = operationType;
        this.deckId = deckId;
        this.playerId = playerId;
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.privateKey = privateKey;
        this.address = address;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public String getOperationType() {
        return operationType;
    }

    public UUID getPlayerId() {return playerId;}

    public UUID getDeckId() {return deckId;}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getAddress() {
        return address;
    }
}
