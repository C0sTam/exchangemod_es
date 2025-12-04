package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.util.CommandUtil;
import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.TransactionUtil;
import net.minecraft.client.MinecraftClient;

public class SendMoneyPacket implements InboundPacket {
    
    private String playerName;
    private int amount;
    private int transactionId;

    
    public String getPlayerName() { return playerName; }
    public int getAmount() { return amount; }
    public int getTransactionId() { return transactionId; }

    @Override
    public void handle() {
        LoggerUtil.info("Received SEND_MONEY packet. Player: " + playerName + ", Amount: " + amount + ", Transaction ID: " + transactionId);

        if (MinecraftClient.getInstance().player != null) {
            TransactionUtil.setLastTransactionId(transactionId);
            if (CommandUtil.isDispatcherReady() && MinecraftClient.getInstance().player  != null && MinecraftClient.getInstance().player .networkHandler.getConnection().isOpen()) {
                MinecraftClient.getInstance().player .networkHandler.sendChatCommand("pay " + playerName + " " + amount);
            }


        }
    }
}