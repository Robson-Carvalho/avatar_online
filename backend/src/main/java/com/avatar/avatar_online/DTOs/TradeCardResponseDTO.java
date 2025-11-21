package com.avatar.avatar_online.DTOs;

public class TradeCardResponseDTO {

    private String transaction;
    private String signer;
    private PlayerTradeInfo player1;
    private PlayerTradeInfo player2;

    public TradeCardResponseDTO() {}

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public PlayerTradeInfo getPlayer1() {
        return player1;
    }

    public void setPlayer1(PlayerTradeInfo player1) {
        this.player1 = player1;
    }

    public PlayerTradeInfo getPlayer2() {
        return player2;
    }

    public void setPlayer2(PlayerTradeInfo player2) {
        this.player2 = player2;
    }

    // Classe interna representando os dados de cada jogador
    public static class PlayerTradeInfo {
        private String address;
        private String receivedCard;
        private int totalCards;

        public PlayerTradeInfo() {}

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getReceivedCard() {
            return receivedCard;
        }

        public void setReceivedCard(String receivedCard) {
            this.receivedCard = receivedCard;
        }

        public int getTotalCards() {
            return totalCards;
        }

        public void setTotalCards(int totalCards) {
            this.totalCards = totalCards;
        }
    }
}
