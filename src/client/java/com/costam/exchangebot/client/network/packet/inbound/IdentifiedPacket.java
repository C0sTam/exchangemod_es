package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.ExchangebotClient;
import com.costam.exchangebot.client.network.packet.outbound.BankUpdatePacket;
import com.costam.exchangebot.client.network.packet.outbound.GetItemsPacket;
import com.costam.exchangebot.client.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class IdentifiedPacket implements InboundPacket {
    private String uuid;
    private String name;
    private String serverIp;
    private String serverId;
    private String mode;
    private double balance;
    private String status;

    
    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public String getServerIp() { return serverIp; }
    public String getServerId() { return serverId; }
    public String getMode() { return mode; }
    public double getBalance() { return balance; }
    public String getStatus() { return status; }


    @Override
    public void handle() {
        LoggerUtil.info("Client successfully identified. UUID: " + uuid + ", Name: " + name + ", Mode: " + mode + ", Balance: " + balance);
        if (MinecraftClient.getInstance().player != null) {

            ExchangebotClient.getWebSocketClient().sendPacket(new GetItemsPacket());
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§a[ExchangeBot] Pomyślnie połączono z systemem!"), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§aTwoje saldo: " + balance + " na trybie: " + mode), false);
        }
    }
}