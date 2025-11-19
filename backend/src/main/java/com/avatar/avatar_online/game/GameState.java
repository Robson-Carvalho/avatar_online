package com.avatar.avatar_online.game;

import com.avatar.avatar_online.models.Card;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String type;
    private String message;
    private final Player playerOne;
    private final Player playerTwo;
    private String turnPlayerId;
    private String playerWin;



    public GameState( String playerOneID, String playerTwoID) {
        this.id = UUID.randomUUID().toString();
        this.message = "Game state updated";
        this.turnPlayerId = ThreadLocalRandom.current().nextBoolean() ? playerOneID : playerTwoID;
        this.playerWin = "void";
        this.playerOne = new Player(playerOneID);
        this.playerTwo = new Player(playerTwoID);
    }

    public void battle() {
        Card cardPlayerOne = this.playerOne.getActivationCardToBattle();
        Card cardPlayerTwo = this.playerTwo.getActivationCardToBattle();

        boolean destroyedByP2 = this.playerOne.reduceLifeCard(cardPlayerTwo.getAttack(), cardPlayerTwo.getElement().toString());
        boolean destroyedByP1 = this.playerTwo.reduceLifeCard(cardPlayerOne.getAttack(), cardPlayerOne.getElement().toString());

        if (destroyedByP1) {
            this.playerOne.addPoint();
        }
        if (destroyedByP2) {
            this.playerTwo.addPoint();
        }

        this.playerOne.setPlayedCard(false);
        this.playerTwo.setPlayedCard(false);

        int pointsP1 = playerOne.getPoints();
        int pointsP2 = playerTwo.getPoints();

        if (pointsP1 >= 3 && pointsP2 >= 3) {
            this.playerWin = "DRAW";

        } else if (pointsP1 >= 3) {
            this.playerWin = playerOne.getId();
        } else if (pointsP2 >= 3) {
            this.playerWin = playerTwo.getId();
        }
    }

    public String getId() { return id; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public void setPlayerWin(String playerWin) { this.playerWin = playerWin; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public Player getPlayerOne() { return playerOne; }

    public Player getPlayerTwo() { return playerTwo; }

    public String getTurnPlayerId() { return turnPlayerId; }

    public void setTurnPlayerId(String turnPlayerId) { this.turnPlayerId = turnPlayerId; }

    public String getPlayerWin() { return playerWin; }
}