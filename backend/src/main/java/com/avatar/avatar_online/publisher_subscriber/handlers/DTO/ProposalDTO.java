package com.avatar.avatar_online.publisher_subscriber.handlers.DTO;

public class ProposalDTO {

    // Cartas do jogador 1
    private String card1ID;
    private String card1Name;
    private String card1Element;
    private String card1Phase;
    private int card1Attack;
    private int card1Life;
    private int card1Defense;
    private String card1Rarity;

    // Cartas do jogador 2
    private String card2ID;
    private String card2Name;
    private String card2Element;
    private String card2Phase;
    private int card2Attack;
    private int card2Life;
    private int card2Defense;
    private String card2Rarity;

    // Jogadores
    private String player1;
    private String player2;

    // Construtor vazio
    public ProposalDTO() {}

    public ProposalDTO(
            String card1ID, String card1Name, String card1Element, String card1Phase,
            int card1Attack, int card1Life, int card1Defense, String card1Rarity,
            String card2ID, String card2Name, String card2Element, String card2Phase,
            int card2Attack, int card2Life, int card2Defense, String card2Rarity,
            String player1, String player2
    ) {
        this.card1ID = card1ID;
        this.card1Name = card1Name;
        this.card1Element = card1Element;
        this.card1Phase = card1Phase;
        this.card1Attack = card1Attack;
        this.card1Life = card1Life;
        this.card1Defense = card1Defense;
        this.card1Rarity = card1Rarity;

        this.card2ID = card2ID;
        this.card2Name = card2Name;
        this.card2Element = card2Element;
        this.card2Phase = card2Phase;
        this.card2Attack = card2Attack;
        this.card2Life = card2Life;
        this.card2Defense = card2Defense;
        this.card2Rarity = card2Rarity;

        this.player1 = player1;
        this.player2 = player2;
    }

    // Getters e Setters
    public String getCard1ID() { return card1ID; }
    public void setCard1ID(String card1ID) { this.card1ID = card1ID; }

    public String getCard1Name() { return card1Name; }
    public void setCard1Name(String card1Name) { this.card1Name = card1Name; }

    public String getCard1Element() { return card1Element; }
    public void setCard1Element(String card1Element) { this.card1Element = card1Element; }

    public String getCard1Phase() { return card1Phase; }
    public void setCard1Phase(String card1Phase) { this.card1Phase = card1Phase; }

    public int getCard1Attack() { return card1Attack; }
    public void setCard1Attack(int card1Attack) { this.card1Attack = card1Attack; }

    public int getCard1Life() { return card1Life; }
    public void setCard1Life(int card1Life) { this.card1Life = card1Life; }

    public int getCard1Defense() { return card1Defense; }
    public void setCard1Defense(int card1Defense) { this.card1Defense = card1Defense; }

    public String getCard1Rarity() { return card1Rarity; }
    public void setCard1Rarity(String card1Rarity) { this.card1Rarity = card1Rarity; }

    public String getCard2ID() { return card2ID; }
    public void setCard2ID(String card2ID) { this.card2ID = card2ID; }

    public String getCard2Name() { return card2Name; }
    public void setCard2Name(String card2Name) { this.card2Name = card2Name; }

    public String getCard2Element() { return card2Element; }
    public void setCard2Element(String card2Element) { this.card2Element = card2Element; }

    public String getCard2Phase() { return card2Phase; }
    public void setCard2Phase(String card2Phase) { this.card2Phase = card2Phase; }

    public int getCard2Attack() { return card2Attack; }
    public void setCard2Attack(int card2Attack) { this.card2Attack = card2Attack; }

    public int getCard2Life() { return card2Life; }
    public void setCard2Life(int card2Life) { this.card2Life = card2Life; }

    public int getCard2Defense() { return card2Defense; }
    public void setCard2Defense(int card2Defense) { this.card2Defense = card2Defense; }

    public String getCard2Rarity() { return card2Rarity; }
    public void setCard2Rarity(String card2Rarity) { this.card2Rarity = card2Rarity; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String player1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }
}
