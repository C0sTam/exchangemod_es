package com.costam.exchangebot.client.network.packet.outbound;


public class GetTransactionPacket implements OutboundPacket {
    private final String type = "TRANSACTION_REQUEST";
    private final Payload payload;

    public GetTransactionPacket(String playerName) {
        this.payload = new Payload(playerName);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public String playerName;

        public Payload(String playerName) {
            this.playerName = playerName;
        }
    }
}