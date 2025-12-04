package com.costam.exchangebot.client.network.packet.outbound;

public class IdentifyPacket implements OutboundPacket {
    private final String type = "IDENTIFY";
    private final Payload payload;

    public IdentifyPacket(String uuid, String name, String serverIp, String serverId, String mode, double balance, String status) {
        this.payload = new Payload(uuid, name, serverIp, serverId, mode, balance, status);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public String uuid;
        public String name;
        public String serverIp;
        public String serverId;
        public String mode;
        public double balance;
        public String status;

        public Payload(String uuid, String name, String serverIp, String serverId, String mode, double balance, String status) {
            this.uuid = uuid;
            this.name = name;
            this.serverIp = serverIp;
            this.serverId = serverId;
            this.mode = mode;
            this.balance = balance;
            this.status = status;
        }
    }
}