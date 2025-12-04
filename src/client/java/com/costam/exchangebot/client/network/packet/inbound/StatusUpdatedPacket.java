package com.costam.exchangebot.client.network.packet.inbound;


import com.costam.exchangebot.client.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class StatusUpdatedPacket implements InboundPacket {
    private String status;
    private boolean connected;

    
    public String getStatus() {
        return status;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void handle() {
        LoggerUtil.info("Status updated: " + status + ", Connected: " + connected);
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§e[ExchangeBot] Twój status został zaktualizowany na: " + status), false);
        }
    }
}