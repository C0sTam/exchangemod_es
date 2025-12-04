package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.event.ClientConnectionEvents;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class AccountJoinPacket implements InboundPacket {
    private String mode;

    @Override
    public void handle() {
        String normalized = normalize(mode);
        ClientConnectionEvents.setReconnectBlockedByCommand(false);
        ServerInfo last = ServerInfoUtil.getLastServerInfo();
        if (last == null || last.address == null || last.address.isEmpty()) return;
        ClientConnectionEvents.requestReconnect();
    }

    private String normalize(String m) {
        if (m == null) return null;
        String s = m.trim().toUpperCase();
        if (s.equals("LF") || s.equals("LIFESTEAL")) return "LIFESTEAL";
        if (s.equals("BOXPVP")) return "BOXPVP";
        return null;
    }
}