package com.costam.exchangebot.client;

import com.costam.exchangebot.client.event.*;
import com.costam.exchangebot.client.network.WebSocketClient;
import com.costam.exchangebot.client.util.ApiClient;
import com.costam.exchangebot.client.util.BalanceInfoUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;

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

    }
    public static WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
}
