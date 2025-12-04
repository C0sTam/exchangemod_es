package com.costam.exchangebot.client.network.packet.outbound;


public class TransactionCreatePacket implements OutboundPacket {
    private final String type = "TRANSACTION_CREATE";
    private final Payload payload;

    public TransactionCreatePacket(String playerName, double amount) {
        this.payload = new Payload(playerName, amount);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public String playerName;
        public double amount;
        public Payload(String playerName, double amount) {
            this.playerName = playerName;
            this.amount = amount;
        }
    }

}