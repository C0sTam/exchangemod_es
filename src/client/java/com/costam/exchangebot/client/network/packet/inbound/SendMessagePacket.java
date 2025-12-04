
package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.util.CommandUtil;
import com.costam.exchangebot.client.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;

public class SendMessagePacket implements InboundPacket {
    
    private String content;

    
    public String getContent() {
        return content;
    }

    @Override
    public void handle() {
        LoggerUtil.info("Received SEND_MESSAGE packet. Content: " + content);

        if (MinecraftClient.getInstance().player != null) {
            if (CommandUtil.isDispatcherReady() &&
                    MinecraftClient.getInstance().player.networkHandler.getConnection().isOpen()) {

                if (content.startsWith("/")) {
                    String messageWithoutSlash = content.substring(1);
                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand(messageWithoutSlash);
                } else {
                    MinecraftClient.getInstance().player.networkHandler.sendChatMessage(content);
                }
            }
        }
    }

}
