package com.costam.exchangebot.client.network.packet.inbound;


import com.costam.exchangebot.client.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ErrorPacket implements InboundPacket {
    private String message;

    public String getMessage() {
        return message;
    }

    @Override
    public void handle() {
        LoggerUtil.error("Received ERROR from server: " + message);
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§c[ExchangeBot] Błąd serwera: " + message), false);
        }
    }
}