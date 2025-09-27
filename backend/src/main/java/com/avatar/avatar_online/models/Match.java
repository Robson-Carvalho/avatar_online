package com.avatar.avatar_online.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


@Entity
public class Match {
    @Id
    private Long id;
    private String playerOneID;
    private String playerTwoID;
    private String playerWin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
