package com.costam.exchangebot.client.event;

import com.costam.exchangebot.client.ExchangebotClient;
import com.costam.exchangebot.client.network.packet.outbound.TransactionCompletedOutboundPacket;
import com.costam.exchangebot.client.network.packet.outbound.TransactionCreatePacket;
import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.TransactionUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class InventoryEventHandler {

    private static final ItemStack[] LAST_INVENTORY = new ItemStack[36];

    
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\$([0-9]+)");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("Wytworzył[: ]+([A-Za-z0-9_]+)");

    
    private static final Map<String, Double> PENDING_TRANSACTIONS = new ConcurrentHashMap<>();
    private static long lastCheckAddTime = 0;
    private static final long TRANSACTION_DELAY_MS = 5000; 

    
    private static final Map<Integer, Boolean> PROCESSED_SLOTS = new ConcurrentHashMap<>();

    
    private record CheckData(String amount, String author) {}



    
    private static boolean running = false;
    private static final int startSlot = 0;   
    private static final int endSlot = 8;     
    private static long delayMs = 400;        
    private static int currentSlot = startSlot;
    private static long lastTime = 0L;
    private static boolean Blocked = false;


    
    private static boolean isActive = false;         
    private static final int firstSlot = 0;          
    private static final int lastSlot = 8;           
    private static long checkDelayMs = 400;          
    private static int slotIndex = firstSlot;        
    private static long lastCheckTimestamp = 0L;


    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            long now = System.currentTimeMillis();

            
            PlayerInventory inv = client.player.getInventory();
            for (int slot = 0; slot < 36; slot++) {
                ItemStack current = inv.getStack(slot);
                ItemStack previous = LAST_INVENTORY[slot];

                if (previous == null || !ItemStack.areEqual(previous, current) ) {
                    if(!Blocked) Blocked = true;
                    handleInventoryChange(client, slot, previous, current);
                    LAST_INVENTORY[slot] = current.copy();
                }
            }

            
            if (!PENDING_TRANSACTIONS.isEmpty() && now - lastCheckAddTime >= TRANSACTION_DELAY_MS) {
                processPendingTransactions(client);
            }

            
            if (running && now - lastTime >= delayMs) {
                lastTime = now;

                inv.selectedSlot = currentSlot;
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(currentSlot));
                }
                if (client.interactionManager != null) {
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                }
                client.player.swingHand(Hand.MAIN_HAND);

                currentSlot++;
                if (currentSlot > endSlot) {
                    currentSlot = startSlot;
                    running = false;
                    Blocked = false;
                }
            }
            if (isActive && now - lastCheckTimestamp >= checkDelayMs) {
                lastCheckTimestamp = now;

                
                List<CheckData> foundChecks = new ArrayList<>();

                if (client.player != null && client.player.getInventory() != null) {
                    for (int slotId = firstSlot; slotId <= lastSlot; slotId++) {
                        int targetSlot = slotId;

                        if (targetSlot < 0 || targetSlot >= client.player.getInventory().size())
                            continue;

                        ItemStack stack = client.player.getInventory().getStack(targetSlot);
                        if (stack == null || stack.isEmpty()) continue;

                        
                        CheckData check = parseCheckItem(stack);
                        if (check != null) {
                            foundChecks.add(check);
                            LoggerUtil.info("Znaleziono czek w slocie " + targetSlot +
                                    " | Kwota: " + check.amount() + ", Autor: " + check.author());
                        }
                    }
                }

                
                if (foundChecks.isEmpty()) {
                    
                    if (ExchangebotClient.getWebSocketClient().isOpen()) {
                        if (TransactionUtil.getLastTransactionId() != null) {
                            ExchangebotClient.getWebSocketClient().sendPacket(
                                    new TransactionCompletedOutboundPacket(
                                            TransactionUtil.getLastTransactionId(),
                                            TransactionUtil.getLastTransactionAmount(),
                                            TransactionUtil.getLastTransactionPlayerName(),
                                            TransactionUtil.getLastTransactionBase64()
                                    )
                            );
                            LoggerUtil.info("Nie znaleziono czeków — wysłano pakiet WebSocket.");
                        }
                    }
                } else {
                    
                    LoggerUtil.info("Znaleziono " + foundChecks.size() + " czek(ów), pomijam wysyłkę WebSocket.");
                }

                
                slotIndex = firstSlot;
                isActive = false;
                TransactionUtil.reset();
                InventoryEventHandler.setRunning(true);
            }
        });
        LoggerUtil.info("[InventoryEventHandler] Inventory event registered.");
    }

    
    private static void processPendingTransactions(MinecraftClient client) {
        LoggerUtil.info("Sending batched transactions...");

        if (!ExchangebotClient.getWebSocketClient().isOpen()) {
            LoggerUtil.warn("WebSocket connection is closed. Batched transactions skipped.");
            PENDING_TRANSACTIONS.clear();
            PROCESSED_SLOTS.clear();
            return;
        }

        PENDING_TRANSACTIONS.forEach((author, totalAmount) -> {
            LoggerUtil.info(String.format("Sending cumulative transaction: author='%s', total_amount=%.2f", author, totalAmount));
            ExchangebotClient.getWebSocketClient().sendPacket(new TransactionCreatePacket(author, totalAmount));
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal(String.format("§7[§bEQ§7] §aWysłano transakcję: §f%s §7na kwotę §e$%.2f", author, totalAmount)),
                        false
                );
            }
        });

        PENDING_TRANSACTIONS.clear();
        PROCESSED_SLOTS.clear();
        Blocked = false;
        lastCheckAddTime = 0;
    }

    private static void handleInventoryChange(MinecraftClient client, int slot, ItemStack oldStack, ItemStack newStack) {
        
        if (PROCESSED_SLOTS.getOrDefault(slot, false)) return;

        String newItemName = (newStack == null || newStack.isEmpty()) ? "pusty" : newStack.getName().getString();


            CheckData check = parseCheckItem(newStack);
            if (newItemName.contains("Czek pieniężny")&&check != null) {
                if (check.author().equals(client.player.getName().getString())) {
                    LoggerUtil.debug("Skipped own check.");
                    return;
                }

                double amount = Double.parseDouble(check.amount());
                PENDING_TRANSACTIONS.merge(check.author(), amount, Double::sum);
                lastCheckAddTime = System.currentTimeMillis();

                PROCESSED_SLOTS.put(slot, true); 

                LoggerUtil.info(String.format("Queued check from '%s' for $%.2f. New total: $%.2f",
                        check.author(), amount, PENDING_TRANSACTIONS.get(check.author())));

                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal(String.format("§7[§bEQ§7] §eDodano do kolejki czek od §f%s §7na kwotę §e$%.2f", check.author(), amount)),
                            false
                    );
                }

                running = true; 
                Blocked = false;
            } else if (!newStack.isEmpty()) {
                String mode = ServerInfoUtil.getServerType();
                if (!"BOXPVP".equals(mode)) {
                    if (client.player != null) {
                        client.player.networkHandler.sendChatCommand("kosz");
                    }
                }
            }

    }

    
    private static CheckData parseCheckItem(ItemStack item) {
        var components = item.getComponents();
        var customName = components.get(DataComponentTypes.CUSTOM_NAME);

        if (customName == null) return null;

        String name = customName.getString();
        var loreComponent = components.get(DataComponentTypes.LORE);

        String amount = null;
        var amountMatcher = AMOUNT_PATTERN.matcher(name);
        if (amountMatcher.find()) {
            amount = amountMatcher.group(1);
        }

        String author = null;
        if (loreComponent != null) {
            var loreLines = loreComponent.lines();
            for (Text line : loreLines) {
                String lineText = line.getString();
                var authorMatcher = AUTHOR_PATTERN.matcher(lineText);
                if (authorMatcher.find()) {
                    author = authorMatcher.group(1);
                    break;
                }
            }
        }

        if (amount == null || author == null) return null;
        return new CheckData(amount, author);
    }
    public static boolean isBlocked() {
        return Blocked;
    }
    public static void setBlocked(boolean blocked) {
        Blocked = blocked;
    }
    public static void setRunning(boolean running) {
        InventoryEventHandler.running = running;
    }
    public static void setIsActive(boolean isActive) {
        InventoryEventHandler.isActive = isActive;
    }

}
