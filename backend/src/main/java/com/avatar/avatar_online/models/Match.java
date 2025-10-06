package com.avatar.avatar_online.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "match_table") 
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_one_id", nullable = false)
    private UserEntity playerOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_two_id", nullable = false)
    private UserEntity playerTwo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_win_id")
    private UserEntity playerWin; // Pode ser null se ainda não houver vencedor

    public Match() {}

    // Getters e setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserEntity getPlayerOne() { return playerOne; }
    public void setPlayerOne(UserEntity playerOne) { this.playerOne = playerOne; }

    public UserEntity getPlayerTwo() { return playerTwo; }
    public void setPlayerTwo(UserEntity playerTwo) { this.playerTwo = playerTwo; }

    public UserEntity getPlayerWin() { return playerWin; }
    public void setPlayerWin(UserEntity playerWin) { this.playerWin = playerWin; }
}
