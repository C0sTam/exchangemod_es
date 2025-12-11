package com.costam.exchangebot.client.network.packet.outbound;

public class FullscreenPacket implements OutboundPacket {
    private final String type = "FULLSCREEN";
    private final Payload payload;

    public FullscreenPacket(int transactionId, double amount, String playerName, String[] images) {
        this.payload = new Payload(transactionId, amount, playerName, images);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public int transactionId;
        public double amount;
        public String playerName;
        public String[] images;

        public Payload(int transactionId, double amount, String playerName, String[] images) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.playerName = playerName;
            this.images = images;
        }
    }
}