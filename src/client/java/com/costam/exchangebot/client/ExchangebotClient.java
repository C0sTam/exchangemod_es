package com.costam.exchangebot.client;

import com.costam.exchangebot.client.event.*;
import com.costam.exchangebot.client.network.WebSocketClient;
import com.costam.exchangebot.client.util.ApiClient;
import com.costam.exchangebot.client.util.BalanceInfoUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ExchangebotClient implements ClientModInitializer {

    private static ApiClient apiClient;

    private static WebSocketClient webSocketClient;
    @Override
    public void onInitializeClient() {
        webSocketClient = new WebSocketClient("ws://localhost:8080");
        webSocketClient.connect();
        String username = MinecraftClient.getInstance().getSession().getUsername();
        ServerInfoUtil.setPreferredModeForUsername(username);
        BalanceInfoUtil.startBankUpdateTask();

        ClientConnectionEvents.register();
        ChatEventHandler.register();
        InventoryEventHandler.register();
        GuiEventHandler.register();
        AutoMoveEventHandler.register();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (client.getSession() != null) {
                client.getWindow().setTitle(client.getSession().getUsername());
            }
            new Thread(() -> {
                try {
                    Thread.sleep(30000); // Changed from 3000 to 30000 (30 seconds)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                client.execute(() -> {
                    if (client.getCurrentServerEntry() == null) {
                        ServerInfo info = new ServerInfo("Anarchia", "anarchia.gg", ServerInfo.ServerType.OTHER);
                        ConnectScreen.connect(client.currentScreen, client, ServerAddress.parse("anarchia.gg"), info, false, null);
                    }
                });
            }).start();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() != null && client.getSession() != null) {
                client.getWindow().setTitle(client.getSession().getUsername());
            }
        });


    }
    public static WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
}