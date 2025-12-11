package com.costam.exchangebot.client.event;

import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.TransactionUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GuiEventHandler {

    private static final Pattern TRADE_GUI_PATTERN = Pattern.compile(":\\|:\\s*([A-Za-z0-9_\\-]+)");
    private static final Pattern KOSZ_GUI_PATTERN = Pattern.compile("Kosz");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\$([0-9]+)");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("Wytworzył[: ]+([A-Za-z0-9_]+)");

    public record CheckData(String amount, String author) {}
    
    private static boolean running = false;
    private static final int startSlot = 0;   
    private static final int endSlot = 8;     
    private static long delayMs = 400;
    private static int currentSlot = startSlot;
    private static long lastTime = 0L;

    private static boolean garbageRunning = false;
    private static final int garbageStartSlot = 54;
    private static final int garbageEndSlot = 89;
    private static int garbageCurrentSlot = garbageStartSlot;
    static long garbageLastTime = 0L;
    private static long garbageDelayMs = 400;





    private static Screen lastScreen = null;
    private static long screenOpenTime = 0L;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void register() {



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            
            Screen currentScreen = client.currentScreen;
            if (currentScreen == null) return;

            
            String title = currentScreen.getTitle().getString();

            if ("Wybierz tryb".equalsIgnoreCase(title)) {
                if (currentScreen != lastScreen) {
                    lastScreen = currentScreen;
                    screenOpenTime = System.currentTimeMillis();
                    scheduler.schedule(() -> {
                        MinecraftClient.getInstance().execute(() -> {
                            if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.currentScreenHandler != null) {
                                String name = MinecraftClient.getInstance().player.getName().getString();
                                int targetSlot = name.equalsIgnoreCase("Batejson") ? 0 : (name.equalsIgnoreCase("Matejson") ? 1 : 0);
                                MinecraftClient.getInstance().interactionManager.clickSlot(
                                        MinecraftClient.getInstance().player.currentScreenHandler.syncId,
                                        targetSlot,
                                        0,
                                        SlotActionType.PICKUP,
                                        MinecraftClient.getInstance().player
                                );
                            }
                        });
                    }, 1, TimeUnit.SECONDS);
                }
            }
            if (title.contains("Kanały")) {
                if (currentScreen != lastScreen) {
                    lastScreen = currentScreen;
                    screenOpenTime = System.currentTimeMillis();
                    scheduler.schedule(() -> {
                        MinecraftClient.getInstance().execute(() -> {
                            if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.currentScreenHandler != null) {
                                ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
                                ItemStack stack10 = handler.getSlot(10).getStack();
                                String n = stack10.isEmpty() ? "" : stack10.getName().getString().toUpperCase();
                                if (n.contains("SPAWN02")) ServerInfoUtil.setDesiredSpawnChannel("SPAWN02");
                                else ServerInfoUtil.setDesiredSpawnChannel("SPAWN01");
                                MinecraftClient.getInstance().interactionManager.clickSlot(
                                        handler.syncId,
                                        10,
                                        0,
                                        SlotActionType.PICKUP,
                                        MinecraftClient.getInstance().player
                                );
                            }
                        });
                    }, 3, TimeUnit.SECONDS);
                }
            }
            
            Matcher matcher = TRADE_GUI_PATTERN.matcher(title);

            if (matcher.find()) {
                String targetName = matcher.group(1);
                if (currentScreen != lastScreen) {
                    lastScreen = currentScreen;
                    screenOpenTime = System.currentTimeMillis();
                } else {
                    long now = System.currentTimeMillis();
                    if (now - screenOpenTime >= 30_000) { 
                        if (client.player != null && client.player.currentScreenHandler != null) {
                            client.player.closeHandledScreen();
                        }
                        lastScreen = null;
                        screenOpenTime = 0L;
                        return;
                    }
                }
                ScreenHandler handler = client.player.currentScreenHandler;

                
                Slot slot53 = handler.getSlot(53);
                ItemStack itemInSlot = slot53.getStack();
                Slot slot45 = handler.getSlot(45);
                ItemStack itemInSlot45 = slot45.getStack();
                
                LoggerUtil.info("[GuiEventHandler] Detected trade GUI with target: " + targetName + ", Item in slot 53: " + itemInSlot);
                if (!itemInSlot.isEmpty() && !itemInSlot45.isEmpty() && TransactionUtil.getLastTransactionPlayerName() != null && targetName.equals(TransactionUtil.getLastTransactionPlayerName()) && itemInSlot45.getItem() != Items.LIME_DYE && itemInSlot.getItem() == Items.RED_DYE) {

                    running = true;

                } else if(running && TransactionUtil.getLastTransactionPlayerName() !=null && targetName.equals(TransactionUtil.getLastTransactionPlayerName()) ) {
                    
                    LoggerUtil.info("[GuiEventHandler] Trade confirmed, closing GUI.");
                    if (client.player != null && client.player.currentScreenHandler != null) {
                        client.player.closeHandledScreen(); 
                    }

                    
                    InventoryEventHandler.setBlocked(true);
                    InventoryEventHandler.setRunning(true);
                } else if (!targetName.equals(TransactionUtil.getLastTransactionPlayerName()) &&!itemInSlot.isEmpty() && itemInSlot.getItem() == Items.LIME_DYE) {

                    if (client.player.currentScreenHandler != null) {
                        boolean validate = validateGuiSlots(client,false);
                        if(!validate) {
                            client.player.closeHandledScreen();
                            InventoryEventHandler.setBlocked(false);
                            return;
                        }
                        client.interactionManager.clickSlot(
                                client.player.currentScreenHandler.syncId,
                                45,
                                0, 
                                SlotActionType.PICKUP, 
                                client.player
                        );
                    }
                }

            }else {
                lastScreen = null;
                screenOpenTime = 0L;
            }
            Matcher garbageMatcher = KOSZ_GUI_PATTERN.matcher(title);
            if(garbageMatcher.find()) {
                LoggerUtil.info("[GuiEventHandler] Detected garbage GUI, starting cleanup.");
                long now = System.currentTimeMillis();

                if (!garbageRunning) {
                    garbageCurrentSlot = garbageStartSlot;
                    garbageRunning = true;
                };

                if (garbageRunning && now - garbageLastTime >= garbageDelayMs) {
                    garbageLastTime = now;

                    int slotId = garbageCurrentSlot;
                    if (client.player.currentScreenHandler != null) {
                        int targetSlot = slotId;
                        if (targetSlot >= 0 && targetSlot <= client.player.currentScreenHandler.slots.size()) {
                            ItemStack stackInSlot = client.player.currentScreenHandler.getSlot(targetSlot).getStack();
                            if (parseCheckItem(stackInSlot) == null&& !stackInSlot.isEmpty()) {
                                client.interactionManager.clickSlot(
                                        client.player.currentScreenHandler.syncId,
                                        targetSlot,
                                        0,
                                        SlotActionType.QUICK_MOVE,
                                        client.player
                                );
                            }else{
                                garbageLastTime = garbageLastTime - garbageDelayMs;
                            }
                        }
                    }

                    garbageCurrentSlot++;
                    if (garbageCurrentSlot > garbageEndSlot) {
                        garbageCurrentSlot = garbageStartSlot;
                        if (client.player.currentScreenHandler != null) {
                            client.player.closeHandledScreen();
                        }
                        garbageRunning = false;
                        scheduler.schedule(() -> {
                            boolean hasGarbage = false;
                            for (int i = 0; i < client.player.getInventory().size(); i++) {
                                ItemStack stack = client.player.getInventory().getStack(i);
                                if (parseCheckItem(stack) == null && !stack.isEmpty()) {
                                    hasGarbage = true;
                                    break;
                                }
                            }
                            if (hasGarbage) {
                                String mode = ServerInfoUtil.getServerType();
                                if (!"BOXPVP".equals(mode)) {
                                    client.player.networkHandler.sendChatCommand("kosz");
                                }
                            } else {
                                InventoryEventHandler.setRunning(true);
                            }
                        }, 500, TimeUnit.MILLISECONDS);
                    }
                }
            }

            long now = System.currentTimeMillis();
            if(!running) currentSlot = startSlot;
            if (running && now - lastTime >= delayMs) {
                lastTime = now;


                int slotId = currentSlot; 
                if (client.player.currentScreenHandler != null) {
                    int targetSlot = 81 + slotId;
                    if (targetSlot >= 0 && targetSlot < client.player.currentScreenHandler.slots.size()) {
                        ItemStack stackInSlot = client.player.currentScreenHandler.getSlot(targetSlot).getStack();
                        if (parseCheckItem(stackInSlot) != null&& !stackInSlot.isEmpty()) {
                            client.interactionManager.clickSlot(
                                    client.player.currentScreenHandler.syncId,
                                    targetSlot,
                                    0,
                                    SlotActionType.QUICK_MOVE,
                                    client.player
                            );
                        }else{
                            lastTime = lastTime - delayMs;
                        }

                    }

                }



                currentSlot++;
                if (currentSlot > endSlot) {
                    currentSlot = startSlot;
                    running = false;
                    ScreenHandler handler = client.player.currentScreenHandler;

                    int totalSlots = handler.slots.size();
                    if (totalSlots < 53) {
                        return;
                    }
                    Slot slot53 = handler.getSlot(53);
                    ItemStack itemInSlot = slot53.getStack();
                    Slot slot45 = handler.getSlot(45);
                    ItemStack itemInSlot45 = slot45.getStack();
                    if (!itemInSlot.isEmpty() && itemInSlot.getItem() == Items.RED_DYE) {
                        if (client.player.currentScreenHandler != null) {
                            boolean validate = validateGuiSlots(client,false);
                            if(!validate) {
                                client.player.closeHandledScreen();
                                return;
                            }

                            client.interactionManager.clickSlot(
                                    client.player.currentScreenHandler.syncId,
                                    45,
                                    0, 
                                    SlotActionType.PICKUP, 
                                    client.player
                            );
                        }

                    } else if (!itemInSlot45.isEmpty() && itemInSlot45.getItem() == Items.RED_DYE) {
                        LoggerUtil.info("[GuiEventHandler] Trade not confirmed or wrong target, closing GUI.");
                        
                        if (client.player != null && client.player.currentScreenHandler != null) {
                            client.player.closeHandledScreen(); 
                        }
                        InventoryEventHandler.setBlocked(true);
                        InventoryEventHandler.setRunning(true);
                    }
                }
            }

        });

        LoggerUtil.info("[GuiEventHandler] GUI event registered.");
    }
    private static boolean validateGuiSlots(MinecraftClient client, boolean onlyAir) {
        if (client.player == null || client.player.currentScreenHandler == null) return false;

        int[] checkSlots = {14, 15, 16, 23, 24, 25, 32, 33, 34};
        int[] emptySlots = {41, 42};

        
        for (int slotIndex : checkSlots) {
            if (slotIndex < 0 || slotIndex >= client.player.currentScreenHandler.slots.size()) {
                InventoryEventHandler.setBlocked(false);
                return false;
            }

            Slot slot = client.player.currentScreenHandler.getSlot(slotIndex);
            ItemStack item = slot.getStack();

            if (onlyAir) {
                if (item != null && item.getItem() != Items.AIR) {
                    InventoryEventHandler.setBlocked(false);
                    return false;
                }
            } else {
                if (item != null && item.getItem() != Items.AIR && parseCheckItem(item) == null) {
                    InventoryEventHandler.setBlocked(false);
                    return false;
                }
            }
        }

        
        for (int slotIndex : emptySlots) {
            if (slotIndex < 0 || slotIndex >= client.player.currentScreenHandler.slots.size()) {
                InventoryEventHandler.setBlocked(false);
                return false;
            }

            Slot slot = client.player.currentScreenHandler.getSlot(slotIndex);
            ItemStack item = slot.getStack();

            if (!item.isEmpty()) {
                InventoryEventHandler.setBlocked(false);
                return false;
            }
        }

        return true;
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
    public static void setCurrentSlot(int currentSlot) {
        GuiEventHandler.currentSlot = currentSlot;
    }
    public static void setRunning(boolean running) {
        GuiEventHandler.running = running;
    }

}
