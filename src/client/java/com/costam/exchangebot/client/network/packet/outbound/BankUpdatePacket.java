package com.costam.exchangebot.client.network.packet.outbound;

public class BankUpdatePacket implements OutboundPacket {
    private final String type = "BANK_UPDATE";
    private final Payload payload;

    public BankUpdatePacket(double balance) {
        this.payload = new Payload(balance);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public double balance;

        public Payload(double balance) {
            this.balance = balance;
        }
    }
}