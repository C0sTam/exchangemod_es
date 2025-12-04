package com.costam.exchangebot.client.network.packet.outbound;


public class GetItemsPacket implements OutboundPacket {
    private final String type = "GET_ITEMS";
    private final Payload payload;

    public GetItemsPacket() {
        this.payload = new Payload();
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
    }
}