package com.costam.exchangebot.client.network.packet.outbound;


public class TransactionCompletedOutboundPacket implements OutboundPacket {
    private final String type = "TRANSACTION_COMPLETED";
    private final Payload payload;

    public TransactionCompletedOutboundPacket(int transactionId, double amount, String playerName, String base64Image) {
        this.payload = new Payload(transactionId, amount, playerName,base64Image);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public int transactionId;
        public double amount;
        public String playerName;
        public String base64Image;

        public Payload(int transactionId, double amount, String playerName, String base64Image) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.playerName = playerName;
            this.base64Image = base64Image;
        }
    }
}