package com.avatar.avatar_online.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "matchs")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_one_id", nullable = false)
    private User playerOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_two_id", nullable = false)
    private User playerTwo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_win_id")
    private User playerWin; // Pode ser null se ainda não houver vencedor

    public Match() {}

    // Getters e setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getPlayerOne() { return playerOne; }
    public void setPlayerOne(User playerOne) { this.playerOne = playerOne; }

    public User getPlayerTwo() { return playerTwo; }
    public void setPlayerTwo(User playerTwo) { this.playerTwo = playerTwo; }

    public User getPlayerWin() { return playerWin; }
    public void setPlayerWin(User playerWin) { this.playerWin = playerWin; }
}
