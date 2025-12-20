package com.costam.exchangebot.client.event;

import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.TransactionUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import com.costam.exchangebot.client.util.ColorStripUtils;
import com.costam.exchangebot.client.ExchangebotClient;
import com.costam.exchangebot.client.network.packet.outbound.TransactionRequestPacket;
import com.costam.exchangebot.client.network.packet.outbound.PlayerStatsPacket;
import com.costam.exchangebot.client.network.packet.outbound.UpdatePricePacket;
import com.costam.exchangebot.client.models.Item;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.client.gui.widget.PressableWidget;
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
    private static final Pattern STATS_GUI_PATTERN = Pattern.compile("Statystyki\\s+([A-Za-z0-9_\\-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RYNEK_GUI_PATTERN = Pattern.compile("Rynek\\s+\\((\\d+)/(\\d+)\\)");
    private static final Pattern MARKET_GUI_PATTERN = Pattern.compile("Market\\s+\\((\\d+)/(\\d+)\\)");
    private static final Pattern PRICE_PATTERN = Pattern.compile("Koszt \\$([0-9,.]+(?:MLN|K)?)\\s*\\(\\$([0-9,]+)\\)", Pattern.CASE_INSENSITIVE);

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

    private static String lastProcessedRynekPage = null;





    private static Screen lastScreen = null;
    private static long screenOpenTime = 0L;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void register() {



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            
            Screen currentScreen = client.currentScreen;
            if (currentScreen == null) return;

            
            String title = currentScreen.getTitle().getString();

            if (title.contains("This server requires") || title.contains("resource pack") || title.contains("paczki zasobów")) {
                if (currentScreen != lastScreen) {
                    lastScreen = currentScreen;
                    screenOpenTime = System.currentTimeMillis();
                    scheduler.schedule(() -> {
                        clickButton(client, currentScreen, "Proceed", "Yes", "Tak", "Zatwierdź", "Akceptuj");
                    }, 500, TimeUnit.MILLISECONDS);
                }
            }

            if (currentScreen instanceof BookScreen || currentScreen instanceof LecternScreen) {
                if (currentScreen != lastScreen) {
                    lastScreen = currentScreen;
                    screenOpenTime = System.currentTimeMillis();
                    scheduler.schedule(() -> {
                        clickButton(client, currentScreen, "Done", "Gotowe", "Zakończ");
                    }, 500, TimeUnit.MILLISECONDS);
                }
            }

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
            if (title.contains("Kanały") && !TransactionUtil.isWaitingStatsConfirmation()) {
                if (currentScreen != lastScreen) {
                    lastScreen = currentScreen;
                    screenOpenTime = System.currentTimeMillis();
                    scheduler.schedule(() -> {
                        MinecraftClient.getInstance().execute(() -> {
                            if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.currentScreenHandler != null) {
                                ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
                                MinecraftClient.getInstance().interactionManager.clickSlot(
                                        handler.syncId,
                                        10,
                                        0,
                                        SlotActionType.PICKUP,
                                        MinecraftClient.getInstance().player
                                );
                            }
                        });
                    }, 1, TimeUnit.SECONDS);
                }
            }
            
            Matcher statsMatcher = STATS_GUI_PATTERN.matcher(title);

            if (statsMatcher.find()) {
                String statsName = statsMatcher.group(1);
                if (TransactionUtil.getPendingStatsPlayerName() != null && statsName.equalsIgnoreCase(TransactionUtil.getPendingStatsPlayerName())) {
                    ScreenHandler handler = client.player.currentScreenHandler;
                    String[] slots = new String[4];
                    int[] idx = {10,11,12,13};
                    for (int i = 0; i < 4; i++) {
                        ItemStack s = handler.getSlot(idx[i]).getStack();
                        slots[i] = s.isEmpty() ? "" : s.getName().getString();
                    }
                    if (ExchangebotClient.getWebSocketClient().isOpen()) {
                        ExchangebotClient.getWebSocketClient().sendPacket(new PlayerStatsPacket(statsName, slots));
                    }
                    if (client.player != null && client.player.currentScreenHandler != null) {
                        client.player.closeHandledScreen();
                    }
                    TransactionUtil.setPendingStatsPlayerName(null);
                    TransactionUtil.setWaitingStatsConfirmation(false);
                    scheduler.schedule(() -> {
                        MinecraftClient.getInstance().execute(() -> {
                            if (ExchangebotClient.getWebSocketClient().isOpen()) {
                                ExchangebotClient.getWebSocketClient().sendPacket(new TransactionRequestPacket(statsName));
                            }
                        });
                    }, 2, TimeUnit.SECONDS);
                    return;
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
                    if(!targetName.equals(TransactionUtil.getLastTransactionPlayerName())) InventoryEventHandler.setRunning(true);


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
            Matcher RynekMatcher = RYNEK_GUI_PATTERN.matcher(title);
            Matcher MarketMatcher = MARKET_GUI_PATTERN.matcher(title);
            boolean isRynek = RynekMatcher.find();
            boolean isMarket = MarketMatcher.find();
            if (isRynek || isMarket) {
                InventoryEventHandler.setBlocked(true);

                String currentPage;
                String totalPages;

                if (isRynek) {
                    currentPage = RynekMatcher.group(1);
                    totalPages = RynekMatcher.group(2);
                } else {
                    currentPage = MarketMatcher.group(1);
                    totalPages = MarketMatcher.group(2);
                }

                // Sprawdź czy ta strona została już przetworzona
                if (currentPage.equals(lastProcessedRynekPage)) {
                    return; // Pomijamy, bo ta strona była już przetworzona
                }

                if(currentPage.equals(totalPages)) {
                    // Oznacz stronę jako przetworzoną
                    lastProcessedRynekPage = currentPage;

                    if (client.player != null && client.player.currentScreenHandler != null) {
                        client.player.closeHandledScreen();
                    }

                    // Wysyłanie UpdatePricePacket dla itemów ze zebranymi statystykami
                    Item[] items = TransactionUtil.getItems();
                    if (items != null && ExchangebotClient.getWebSocketClient().isOpen()) {
                        for (Item item : items) {
                            if (item.getLowestPrice() != null && item.getItemCount() != null && item.getItemCount() > 0) {
                                ExchangebotClient.getWebSocketClient().sendPacket(
                                    new UpdatePricePacket(
                                        item.getId(),
                                        item.getLowestPrice(),
                                        item.getHighestPrice(),
                                            (double) item.getTotalPrice() / item.getItemCount(),
                                        item.getTotalPrice(),
                                        item.getItemCount()
                                    )
                                );
                                item.setLowestPrice(null);
                                item.setHighestPrice(null);
                                item.setTotalPrice(null);
                                item.setItemCount(0);
                            }
                        }
                    }
                    InventoryEventHandler.setBlocked(false);
                    // Zresetuj po zakończeniu całego procesu
                    lastProcessedRynekPage = null;
                } else {
                    // Przeszukiwanie slotów w GUI Rynek (nie ostatnia strona)
                    Item[] items = TransactionUtil.getItems();
                    if (items != null && items.length > 0 && client.player.currentScreenHandler != null) {
                        int[] slotsToCheck = {2, 3, 4, 5, 6, 7, 8, 11, 12, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 34, 35};

                        for (int slotIndex : slotsToCheck) {
                            if (slotIndex >= client.player.currentScreenHandler.slots.size()) continue;

                            ItemStack stackInSlot = client.player.currentScreenHandler.getSlot(slotIndex).getStack();
                            if (stackInSlot.isEmpty()) continue;

                            // Pobierz informacje o itemie ze slotu
                            String slotMaterial = stackInSlot.getItem().toString().toLowerCase();
                            LoggerUtil.info("[GuiEventHandler] Slot " + slotIndex + " material format: " + slotMaterial);
                            var customNameComponent = stackInSlot.getComponents().get(DataComponentTypes.CUSTOM_NAME);
                            String slotName = customNameComponent != null ? ColorStripUtils.stripAllColorsAndFormats(customNameComponent.getString()).toLowerCase() : null;
                            var loreComponent = stackInSlot.getComponents().get(DataComponentTypes.LORE);
                            String slotLore = null;
                            if (loreComponent != null && !loreComponent.lines().isEmpty()) {
                                // Pobierz wszystkie linie lore i połącz je w jeden string
                                StringBuilder loreBuilder = new StringBuilder();
                                for (int i = 0; i < loreComponent.lines().size(); i++) {
                                    if (i > 0) loreBuilder.append(" ");
                                    loreBuilder.append(ColorStripUtils.stripAllColorsAndFormats(loreComponent.lines().get(i).getString()));
                                }
                                slotLore = loreBuilder.toString().toLowerCase();
                            }
                            Integer slotCustomModelData = null;
                            try {
                                var customModelDataComponent = stackInSlot.getComponents().get(DataComponentTypes.CUSTOM_MODEL_DATA);
                                if (customModelDataComponent != null) {
                                    // Używamy reflection bo CustomModelDataComponent.value() może nie być dostępny w compile-time
                                    slotCustomModelData = (Integer) customModelDataComponent.getClass().getMethod("value").invoke(customModelDataComponent);
                                }
                            } catch (Exception e) {
                                // Ignorujemy błędy - customModelData pozostanie null
                            }

                            // Wyciągnij cenę z lore
                            Double slotPrice = extractPriceFromLore(loreComponent);

                            if (slotPrice == null) {
                                continue;
                            };

                            // Szukaj dopasowania w items
                            for (Item item : items) {

                                boolean matches = true;

                                // Przygotuj oczyszczone wartości z item dla porównania (bez formatowania, małe litery)
                                String cleanItemMaterial = item.getMaterial() != null ? ColorStripUtils.stripAllColorsAndFormats(item.getMaterial()).toLowerCase() : null;
                                String cleanItemName = item.getName() != null ? ColorStripUtils.stripAllColorsAndFormats(item.getName()).toLowerCase() : null;
                                String cleanItemLore = item.getLore() != null ? ColorStripUtils.stripAllColorsAndFormats(item.getLore()).toLowerCase() : null;

                                // Sprawdź material
                                if (cleanItemMaterial != null && !slotMaterial.contains(cleanItemMaterial)) {
                                    matches = false;
                                }

                                // Sprawdź name
                                if (matches && cleanItemName != null && slotName != null && !slotName.contains(cleanItemName)) {
                                    matches = false;
                                }

                                // Sprawdź lore
                                if (matches && cleanItemLore != null && slotLore != null && !slotLore.contains(cleanItemLore)) {
                                    matches = false;
                                }

                                // Sprawdź customModelData (tylko jeśli nie jest null w item)
                                if (matches && item.getCustomModelData() != null) {
                                    if (slotCustomModelData == null || !slotCustomModelData.equals(item.getCustomModelData())) {
                                        matches = false;
                                    }
                                }

                                // Jeśli dopasowanie - aktualizuj statystyki
                                if (matches) {
                                    int slotPriceInt = slotPrice.intValue();

                                    // Zwiększ licznik itemów
                                    int currentCount = item.getItemCount() != null ? item.getItemCount() : 0;
                                    item.setItemCount(currentCount + 1);

                                    // Aktualizuj najniższą cenę
                                    Integer currentLowestPrice = item.getLowestPrice();
                                    if (currentLowestPrice == null || slotPriceInt < currentLowestPrice) {
                                        item.setLowestPrice(slotPriceInt);
                                        LoggerUtil.info("[GuiEventHandler] Updated lowestPrice for itemId=" + item.getId() +
                                            ", newPrice=" + slotPriceInt + " (slot " + slotIndex + ")");
                                    }

                                    // Aktualizuj najwyższą cenę
                                    Integer currentHighestPrice = item.getHighestPrice();
                                    if (currentHighestPrice == null || slotPriceInt > currentHighestPrice) {
                                        item.setHighestPrice(slotPriceInt);
                                        LoggerUtil.info("[GuiEventHandler] Updated highestPrice for itemId=" + item.getId() +
                                            ", newPrice=" + slotPriceInt + " (slot " + slotIndex + ")");
                                    }

                                    // Aktualizuj totalPrice
                                    Long currentTotalPrice = item.getTotalPrice() != null ? item.getTotalPrice() : 0L;
                                    item.setTotalPrice(currentTotalPrice + slotPriceInt);

                                }

                            }
                        }

                        // Oznacz bieżącą stronę jako przetworzoną przed przejściem do następnej
                        lastProcessedRynekPage = currentPage;

                        // Po przejrzeniu wszystkich slotów, kliknij slot 50 (następna strona)
                        if (client.player.currentScreenHandler != null && client.player.currentScreenHandler.slots.size() > 50) {
                            LoggerUtil.info("[GuiEventHandler] Finished checking slots, clicking slot 50 (next page)");
                            client.interactionManager.clickSlot(
                                client.player.currentScreenHandler.syncId,
                                50,
                                0,
                                SlotActionType.PICKUP,
                                client.player
                            );
                        }
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
                if (item != null && item.getItem() != Items.AIR) {
                    CheckData checkData = parseCheckItem(item);
                    if (checkData == null || checkData.author().equalsIgnoreCase(client.player.getName().getString())) {
                        InventoryEventHandler.setBlocked(false);
                        return false;
                    }
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

    private static Double extractPriceFromLore(net.minecraft.component.type.LoreComponent loreComponent) {
        if (loreComponent == null) return null;

        for (Text line : loreComponent.lines()) {
            String lineText = ColorStripUtils.stripAllColorsAndFormats(line.getString());
            Matcher priceMatcher = PRICE_PATTERN.matcher(lineText);
            if (priceMatcher.find()) {
                // Użyj drugiej grupy (dokładna wartość w nawiasach)
                String priceStr = priceMatcher.group(2).replace(",", "");
                try {
                    return Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    LoggerUtil.warn("[GuiEventHandler] Failed to parse price: " + priceStr);
                }
            }
        }
        return null;
    }

    public static void setCurrentSlot(int currentSlot) {
        GuiEventHandler.currentSlot = currentSlot;
    }
    public static void setRunning(boolean running) {
        GuiEventHandler.running = running;
    }

    private static void clickButton(MinecraftClient client, Screen screen, String... buttonTexts) {
        client.execute(() -> {
            if (screen == null) return;
            for (Element element : screen.children()) {
                if (element instanceof PressableWidget button) {
                    String buttonMessage = button.getMessage().getString();
                    for (String text : buttonTexts) {
                        if (buttonMessage.equalsIgnoreCase(text)) {
                            button.onPress();
                            return;
                        }
                    }
                }
            }
        });
    }

}
