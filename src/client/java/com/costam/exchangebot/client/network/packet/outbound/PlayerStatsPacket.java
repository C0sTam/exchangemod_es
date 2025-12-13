package com.costam.exchangebot.client.network.packet.outbound;

public class PlayerStatsPacket implements OutboundPacket {
    private final String type = "PLAYER_STATS";
    private final Payload payload;

    public PlayerStatsPacket(String playerName, String[] slots) {
        this.payload = new Payload(playerName, slots);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public String playerName;
        public String[] slots;

        public Payload(String playerName, String[] slots) {
            this.playerName = playerName;
            this.slots = slots;
        }
    }
}