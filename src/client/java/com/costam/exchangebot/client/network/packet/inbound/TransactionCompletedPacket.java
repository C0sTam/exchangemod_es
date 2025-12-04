package com.costam.exchangebot.client.network.packet.inbound;


import com.costam.exchangebot.client.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TransactionCompletedPacket implements InboundPacket {
    private String transactionId;
    private int amount;
    private String playerName; 
    private String status;

    
    public String getTransactionId() {
        return transactionId;
    }

    public int getAmount() {
        return amount;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public void handle() {
        LoggerUtil.info("Transaction " + transactionId + " completed for player " + playerName + ". Amount: " + amount + ", Status: " + status);
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§b[ExchangeBot] Transakcja " + transactionId + " zakończona dla gracza " + playerName + ". Kwota: " + amount), false);
            if ("completed".equals(status)) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§aTransakcja pomyślnie zakończona!"), false);
            } else {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§cTransakcja wciąż w toku lub z innym statusem: " + status), false);
            }
        }
    }
}