package com.avatar.avatar_online.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PackDTO {
    @JsonProperty("PlayerId")
    private String PlayerId;

    public PackDTO() {}

    public PackDTO(String playerId) {
        PlayerId = playerId;
    }

    public String getPlayerId() {
        return PlayerId;
    }

    public void setPlayerId(String playerId) {
        PlayerId = playerId;
    }
}
