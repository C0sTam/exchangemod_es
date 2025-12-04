package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.event.InventoryEventHandler;
import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import com.costam.exchangebot.client.util.TransactionUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

public class TransactionRejectedPacket implements InboundPacket {
    
    
    private String playerName;
    private double amount;
    private String sourceMode;
    private String targetMode;
    private double toAmount;
    private String transactionId;
    private String status;
    private double alreadySentAmount;
    private List<Map<String, String>> statusMessages; 
    private String reason;

    
    public String getPlayerName() { return playerName; }
    public double getAmount() { return amount; }
    public String getSourceMode() { return sourceMode; }
    public String getTargetMode() { return targetMode; }
    public double getToAmount() { return toAmount; }
    public String getTransactionId() { return transactionId; }
    public String getStatus() { return status; }
    public double getAlreadySentAmount() { return alreadySentAmount; }
    public List<Map<String, String>> getStatusMessages() { return statusMessages; }
    public String getReason() { return reason; }

    @Override
    public void handle() {
        LoggerUtil.warn("Transaction rejected for player " + playerName + ". Reason: " + reason + " (ID: " + transactionId + ", Status: " + status + ")");
        if( InventoryEventHandler.isBlocked()) InventoryEventHandler.setBlocked(false);
        if (MinecraftClient.getInstance().player != null) {
            if (status.equals("not_found_lifesteal") || (reason.contains("Niewystarczające środki")&& ServerInfoUtil.getServerType().equals("LIFESTEAL"))) {
                String finalCmd = "wymiana " + playerName;
                MinecraftClient.getInstance().player .networkHandler.sendChatCommand(finalCmd);
            }else {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§c[ExchangeBot] Transakcja odrzucona dla " + playerName + " (ID: " + transactionId + ")"), false);
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§cPowód: " + reason), false);
                if (statusMessages != null && !statusMessages.isEmpty()) {
                    statusMessages.forEach(msg -> MinecraftClient.getInstance().player.sendMessage(Text.literal("§7 - " + msg.get("message")), false));
                }
                InventoryEventHandler.setBlocked(false);
                TransactionUtil.reset();;
            }



        }
    }
}