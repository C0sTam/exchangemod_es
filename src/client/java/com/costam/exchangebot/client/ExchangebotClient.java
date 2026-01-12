package com.costam.exchangebot.client;

import com.costam.exchangebot.client.event.*;
import com.costam.exchangebot.client.automation.MenuPrevention;
import com.costam.exchangebot.client.network.WebSocketClient;
import com.costam.exchangebot.client.util.ApiClient;
import com.costam.exchangebot.client.util.BalanceInfoUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ExchangebotClient implements ClientModInitializer {

    private static ApiClient apiClient;

    private static WebSocketClient webSocketClient;
    private static KeyBinding menuPreventionKey;

    @Override
    public void onInitializeClient() {
        webSocketClient = new WebSocketClient("ws://localhost:8081");
        webSocketClient.connect();
        String username = MinecraftClient.getInstance().getSession().getUsername();
        ServerInfoUtil.setPreferredModeForUsername(username);
        BalanceInfoUtil.startBankUpdateTask();

        ClientConnectionEvents.register();
        ChatEventHandler.register();
        InventoryEventHandler.register();
        GuiEventHandler.register();
        AutoMoveEventHandler.register();

        // Register Menu Prevention
        MenuPrevention.register();
        menuPreventionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.exchangebot.menu_prevention",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.exchangebot.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() != null && client.getSession() != null) {
                client.getWindow().setTitle(client.getSession().getUsername());
            }
            
            // Handle Menu Prevention Toggle
            while (menuPreventionKey.wasPressed()) {
                MenuPrevention.toggleBackgroundPrevention();
            }
            // Enforce cursor state
            MenuPrevention.onTick(client);
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MenuPrevention.renderIcon(context);
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (client.getSession() != null) {
                client.getWindow().setTitle(client.getSession().getUsername());
            }
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                client.execute(() -> {
                    if (client.getCurrentServerEntry() == null) {
                        client.setScreen(new MultiplayerScreen(new TitleScreen()));
                    }
                });

                try {
                    Thread.sleep(2000); // Changed from 1000 to 2000
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




    }
    public static WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
}