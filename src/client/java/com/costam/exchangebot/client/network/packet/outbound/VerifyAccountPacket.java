package com.costam.exchangebot.client.network.packet.outbound;

public class VerifyAccountPacket implements OutboundPacket {
    private final String type = "VERIFY_ACCOUNT";
    private final Payload payload;

    public VerifyAccountPacket(String minecraftNick, String code) {
        this.payload = new Payload(minecraftNick, code);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public String minecraftNick;
        public String code;

        public Payload(String minecraftNick, String code) {
            this.minecraftNick = minecraftNick;
            this.code = code;
        }
    }
}

