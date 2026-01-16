package com.costam.exchangebot.client.event;


import com.costam.exchangebot.client.ExchangebotClient;
import com.costam.exchangebot.client.network.WebSocketClient;
import com.costam.exchangebot.client.network.packet.outbound.IdentifyPacket;
import com.costam.exchangebot.client.network.packet.outbound.StatusUpdatePacket;
import com.costam.exchangebot.client.util.BalanceInfoUtil;
import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import com.costam.exchangebot.client.util.TransactionUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientConnectionEvents {

    static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static volatile boolean reconnecting = false;
    private static int reconnectAttempts = 0;
    private static final int RECONNECT_DELAY_SECONDS = 3;
    private static final int CONNECT_TIMEOUT_SECONDS = 60;
    private static volatile boolean reconnectBlockedByCommand = false;
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerInfoUtil.captureCurrentServer();
            reconnecting = false;
            reconnectAttempts = 0;
            reconnectBlockedByCommand = false;
            if (client.player != null
                    && ServerInfoUtil.getServerAddress() != null
                    && !ServerInfoUtil.getServerAddress().equals("singleplayer")) {
                String name = client.player.getName().getString();
                client.player.sendMessage(Text.literal("[ExchangeBot] Account: " + name), false);
            }
            if (client.player != null
                    && ServerInfoUtil.getServerAddress() != null
                    && !ServerInfoUtil.getServerAddress().equals("singleplayer")
                    && ServerInfoUtil.getServerBrand() != null
                    && ServerInfoUtil.getServerType() != null) {

                LoggerUtil.info("Player joined the game. Attempting to identify with WebSocket server.");
                WebSocketClient wsClient = ExchangebotClient.getWebSocketClient();

                if (wsClient != null && wsClient.isOpen()) {
                    ClientPlayerEntity player = client.player;

                    InventoryEventHandler.setBlocked(false);
                    TransactionUtil.reset();
                    String modeNow = ServerInfoUtil.getServerType();
                    if ("LIFESTEAL".equals(modeNow)) {
                        ServerInfoUtil.markLifestealEnter();
                    } else {
                        ServerInfoUtil.resetLifestealEnter();
                    }

                    scheduler.schedule(() -> {
                        try {
                            String uuid = player.getUuidAsString();
                            String name = player.getName().getString();
                            String serverIp = ServerInfoUtil.getServerAddress();
                            String serverId = ServerInfoUtil.getServerBrand();
                            String mode = ServerInfoUtil.getServerType();
                            Double balance = BalanceInfoUtil.getBalance();
                            String status = "IN_GAME";

                            wsClient.sendPacket(new IdentifyPacket(uuid, name, serverIp, serverId, mode, balance, status));

                            InventoryEventHandler.setBlocked(false);
                        } catch (Exception e) {
                            LoggerUtil.error("Failed to send IDENTIFY packet: " + e.getMessage());
                        }
                    }, 3, TimeUnit.SECONDS);
                } else {
                    LoggerUtil.warn("WebSocket client not connected or initialized when player joined. Cannot send IDENTIFY packet.");
                }

                scheduler.schedule(() -> {
                    try {
                        String mode = ServerInfoUtil.getServerType();
                        if ("LIFESTEAL".equals(mode)) {
                            if (!ServerInfoUtil.isLifestealInCooldown()) {
                                ServerInfoUtil.markLifestealEnter();
                            }
                        }
                    } catch (Exception ignored) { }
                }, 3, TimeUnit.SECONDS);

                scheduler.schedule(() -> {
                    try {
                        String mode = ServerInfoUtil.getServerType();
                        if ("LIFESTEAL".equals(mode) || "BOXPVP".equals(mode)) {
                            client.execute(() -> {
                                if (client.currentScreen != null) {
                                    String cls = client.currentScreen.getClass().getName();
                                    String title = client.currentScreen.getTitle() != null ? client.currentScreen.getTitle().getString() : "";
                                    String lower = title.toLowerCase();
                                    boolean isBook = cls.contains("BookScreen") || lower.contains("book") || lower.contains("książ");
                                    if (isBook) {
                                        MinecraftClient.getInstance().setScreen(null);
                                    }
                                }
                            });
                        }
                    } catch (Exception ignored) { }
                }, 3, TimeUnit.SECONDS);

                scheduler.schedule(() -> {
                    try {
                        String mode = ServerInfoUtil.getServerType();
                        if ("BOXPVP".equals(mode)) {
                            boolean needCh = !ServerInfoUtil.isSpawn01ChannelOnScoreboard();
                            if (needCh) {
                                scheduler.schedule(() -> {}, 7, TimeUnit.SECONDS);
                                scheduler.schedule(() -> {
                                    client.execute(() -> {
                                        if (client.player != null && client.player.networkHandler != null && client.player.networkHandler.getConnection().isOpen()) {
                                            client.player.networkHandler.sendChatCommand("bezpieczna");
                                        }
                                    });
                                }, 12, TimeUnit.SECONDS);
                            } else {
                                scheduler.schedule(() -> {
                                    client.execute(() -> {
                                        if (client.player != null && client.player.networkHandler != null && client.player.networkHandler.getConnection().isOpen()) {
                                            client.player.networkHandler.sendChatCommand("bezpieczna");
                                        }
                                    });
                                }, 5, TimeUnit.SECONDS);
                            }
                        }
                    } catch (Exception ignored) { }
                }, 3, TimeUnit.SECONDS);




            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            
            LoggerUtil.info("Player disconnected from the game.");
            WebSocketClient wsClient = ExchangebotClient.getWebSocketClient();
            if (wsClient != null && wsClient.isOpen()) {
                
                
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    wsClient.sendPacket(new StatusUpdatePacket("OFFLINE", false));
                }
            }

            ServerInfo last = ServerInfoUtil.getLastServerInfo();
            if (!reconnectBlockedByCommand && last != null && last.address != null && !last.address.isEmpty()) {
                triggerReconnect(last);
            }
        });
    }

    public static void requestReconnect() {
        ServerInfo last = ServerInfoUtil.getLastServerInfo();
        if (!reconnectBlockedByCommand && last != null && last.address != null && !last.address.isEmpty()) {
            triggerReconnect(last);
        }
    }

    private static void triggerReconnect(ServerInfo last) {
        reconnecting = true;
        scheduler.schedule(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        if (mc.world != null || mc.getNetworkHandler() != null) {
                            reconnecting = false;
                            reconnectAttempts = 0;
                            return;
                        }
                        String screenName = mc.currentScreen != null ? mc.currentScreen.getClass().getName() : "";
                        if (screenName.contains("ConnectScreen")) {
                            return;
                        }
                        ServerAddress addr = ServerAddress.parse(last.address);
                        ConnectScreen.connect(mc.currentScreen, mc, addr, last, false, null);
                        LoggerUtil.info("Scheduled reconnect to server: " + last.address);
                        scheduler.schedule(() -> {
                            MinecraftClient mc2 = MinecraftClient.getInstance();
                            if (mc2 != null) {
                                mc2.execute(() -> {
                                    if (mc2.world != null || mc2.getNetworkHandler() != null) {
                                        reconnecting = false;
                                        reconnectAttempts = 0;
                                        return;
                                    }
                                    String s2 = mc2.currentScreen != null ? mc2.currentScreen.getClass().getName() : "";
                                    boolean connecting = s2.contains("ConnectScreen");
                                    if (!connecting && reconnecting && !reconnectBlockedByCommand) {
                                        reconnectAttempts++;
                                        triggerReconnect(last);
                                    }
                                });
                            }
                        }, CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        LoggerUtil.error("Reconnect failed: " + e.getMessage());
                    }
                });
            }
        }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    public static void setReconnectBlockedByCommand(boolean blocked) {
        reconnectBlockedByCommand = blocked;
    }

    public static boolean isReconnectBlockedByCommand() {
        return reconnectBlockedByCommand;
    }


}
