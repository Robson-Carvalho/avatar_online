package com.avatar.avatar_online.publisher_subscriber.model;

public class OnlineUserDTO {
    private String id;
    private String nickname;

    public OnlineUserDTO() {}

    public OnlineUserDTO(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
