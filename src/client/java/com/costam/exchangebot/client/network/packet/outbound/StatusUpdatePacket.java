package com.costam.exchangebot.client.network.packet.outbound;

public class StatusUpdatePacket implements OutboundPacket {
    private final String type = "STATUS_UPDATE";
    private final Payload payload;

    public StatusUpdatePacket(String status, boolean connected) {
        this.payload = new Payload(status, connected);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public String status;
        public boolean connected;

        public Payload(String status, boolean connected) {
            this.status = status;
            this.connected = connected;
        }
    }
}