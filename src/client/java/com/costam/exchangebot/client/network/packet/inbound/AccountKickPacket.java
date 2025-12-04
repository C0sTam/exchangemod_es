package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.event.ClientConnectionEvents;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class AccountKickPacket implements InboundPacket {
    private String mode;

    @Override
    public void handle() {
        String currentMode = ServerInfoUtil.getServerType();
        if (currentMode == null) return;
        if (!matchesMode(currentMode)) return;
        ClientConnectionEvents.setReconnectBlockedByCommand(true);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                if (mc.world != null) {
                    mc.world.disconnect();
                }
            });
        }
    }

    private boolean matchesMode(String currentMode) {
        String normalized = normalize(mode);
        return normalized != null && normalized.equalsIgnoreCase(currentMode);
    }

    private String normalize(String m) {
        if (m == null) return null;
        String s = m.trim().toUpperCase();
        if (s.equals("LF") || s.equals("LIFESTEAL")) return "LIFESTEAL";
        if (s.equals("BOXPVP")) return "BOXPVP";
        return null;
    }
}