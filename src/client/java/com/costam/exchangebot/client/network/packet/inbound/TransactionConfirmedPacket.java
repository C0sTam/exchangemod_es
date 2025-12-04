package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

public class TransactionConfirmedPacket implements InboundPacket {
    
    
    private String playerName;
    private double amount;
    private String sourceMode;
    private String targetMode;
    private double toAmount;
    private String transactionId;
    private String status;
    private double alreadySentAmount;
    private List<Map<String, String>> statusMessages; 

    
    public String getPlayerName() { return playerName; }
    public double getAmount() { return amount; }
    public String getSourceMode() { return sourceMode; }
    public String getTargetMode() { return targetMode; }
    public double getToAmount() { return toAmount; }
    public String getTransactionId() { return transactionId; }
    public String getStatus() { return status; }
    public double getAlreadySentAmount() { return alreadySentAmount; }
    public List<Map<String, String>> getStatusMessages() { return statusMessages; }


    @Override
    public void handle() {
        LoggerUtil.info("Transaction confirmed! ID: " + transactionId + ", To: " + playerName + ", Amount: " + amount + " (" + sourceMode + " -> " + toAmount + " " + targetMode + ")");
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§a[ExchangeBot] Transakcja potwierdzona! ID: " + transactionId), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§aPrzesyłasz " + amount + " " + sourceMode + " do " + playerName + " na " + targetMode + " (przeliczone na " + toAmount + " " + targetMode + ")"), false);
            if (statusMessages != null && !statusMessages.isEmpty()) {
                statusMessages.forEach(msg -> MinecraftClient.getInstance().player.sendMessage(Text.literal("§7 - " + msg.get("message")), false));
            }
        }
    }
}