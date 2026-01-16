package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.event.InventoryEventHandler;
import com.costam.exchangebot.client.util.AmountSplitter;
import com.costam.exchangebot.client.util.CommandUtil;
import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.TransactionUtil;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SendTradePacket implements InboundPacket {
    private String playerName;
    private int amount;
    private int transactionId;

    public String getPlayerName() { return playerName; }
    public int getAmount() { return amount; }
    public int getTransactionId() { return transactionId; }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void handle() {
        LoggerUtil.info("Received SEND_MONEY packet. Player: " + playerName + ", Amount: " + amount + ", Transaction ID: " + transactionId);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (InventoryEventHandler.isBlocked() || client.currentScreen != null) return;
        if (!CommandUtil.isDispatcherReady() || !client.player.networkHandler.getConnection().isOpen()) return;

        TransactionUtil.setLastTransactionId(transactionId);
        TransactionUtil.setLastTransactionPlayerName(playerName);
        TransactionUtil.setLastTransactionAmount(amount);

        List<Long> splitAmounts = AmountSplitter.splitAmount(amount);
        LoggerUtil.info("Split amount into " + splitAmounts.size() + " parts: " + splitAmounts);

        int delaySeconds = 2;
        int totalParts = splitAmounts.size();

        
        for (int i = 0; i < totalParts; i++) {
            long amountPart = splitAmounts.get(i);
            int finalI = i;

            scheduler.schedule(() -> {
                String command = "czek " + amountPart;
                MinecraftClient.getInstance().player .networkHandler.sendChatCommand(command);
                LoggerUtil.info("Executed command: " + command);

                
                if (finalI == totalParts - 1) {
                    scheduler.schedule(() -> {
                        InventoryEventHandler.setBlocked(false);
                        String finalCmd = "wymiana " + playerName;
                        MinecraftClient.getInstance().player .networkHandler.sendChatCommand(finalCmd);
                        LoggerUtil.info("Executed final command: " + finalCmd);
                    }, delaySeconds*2, TimeUnit.SECONDS);
                }
            }, (long) delaySeconds * i, TimeUnit.SECONDS);
        }
    }
}
