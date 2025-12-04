package com.costam.exchangebot.client.network;

import com.costam.exchangebot.client.ExchangebotClient;
import com.costam.exchangebot.client.network.packet.inbound.*;
import com.costam.exchangebot.client.network.packet.outbound.GetItemsPacket;
import com.costam.exchangebot.client.network.packet.outbound.IdentifyPacket;
import com.costam.exchangebot.client.network.packet.outbound.OutboundPacket;
import com.costam.exchangebot.client.util.BalanceInfoUtil;
import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

    private final Gson gson = new Gson();
    private final Map<String, Class<? extends InboundPacket>> inboundPacketTypes = new HashMap<>();
    private final ScheduledExecutorService reconnectionScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService bankUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
    private int reconnectAttempts = 0;



    private boolean isOpen = false;

    public WebSocketClient(String serverUri) {
        super(createUri(serverUri));
        if (uri == null) {
            throw new IllegalArgumentException("Invalid server URI: " + serverUri);
        }
        setupPacketTypes();
    }

    private static URI createUri(String uriString) {
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            LoggerUtil.error("Invalid URI syntax: " + uriString, e);
            return null;
        }
    }

    private void setupPacketTypes() {
        inboundPacketTypes.put("BANK_UPDATE_CONFIRMED", BankUpdateConfirmedPacket.class);
        inboundPacketTypes.put("IDENTIFIED", IdentifiedPacket.class);
        inboundPacketTypes.put("TRANSACTION_CONFIRMED", TransactionConfirmedPacket.class);
        inboundPacketTypes.put("TRANSACTION_REJECTED", TransactionRejectedPacket.class);
        inboundPacketTypes.put("TRANSACTION_COMPLETED", TransactionCompletedPacket.class);
        inboundPacketTypes.put("STATUS_UPDATED", StatusUpdatedPacket.class);
        inboundPacketTypes.put("SEND_MONEY", SendMoneyPacket.class);
        inboundPacketTypes.put("SEND_TRADE", SendTradePacket.class);
        inboundPacketTypes.put("SEND_MESSAGE", SendMessagePacket.class);
        inboundPacketTypes.put("ACCOUNT_KICK", AccountKickPacket.class);
        inboundPacketTypes.put("ACCOUNT_JOIN", AccountJoinPacket.class);
        inboundPacketTypes.put("ITEMS_RESPONSE", ItemsResponsePacket.class);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LoggerUtil.info("Connected to WebSocket server.");
        isOpen = true;
        reconnectAttempts = 0;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null && ServerInfoUtil.getServerAddress() != null && ServerInfoUtil.getServerAddress() != "singleplayer" && ServerInfoUtil.getServerBrand() != null && ServerInfoUtil.getServerType() != null) {
            LoggerUtil.info("Player joined the game. Attempting to identify with WebSocket server.");
            WebSocketClient wsClient = ExchangebotClient.getWebSocketClient();
            if (wsClient != null && wsClient.isOpen()) {
                ClientPlayerEntity player = client.player;

                String uuid = player.getUuidAsString();
                String name = player.getName().getString();
                String serverIp = ServerInfoUtil.getServerAddress(); 
                String serverId = ServerInfoUtil.getServerBrand(); 
                String mode = ServerInfoUtil.getServerType(); 
                Double balance = BalanceInfoUtil.getBalance();
                String status = "IN_GAME"; 

                wsClient.sendPacket(new IdentifyPacket(uuid, name, serverIp, serverId, mode, balance, status));

            } else {
                LoggerUtil.warn("WebSocket client not connected or initialized when player joined. Cannot send IDENTIFY packet.");
            }
        }
    }

    @Override
    public void onMessage(String message) {
        LoggerUtil.debug("Received message: " + message);
        try {
            Map<String, Object> genericMessage = gson.fromJson(message, Map.class);
            String type = (String) genericMessage.get("type");
            if (type != null) type = type.trim().toUpperCase();
            Object payloadObj = genericMessage.get("payload");

            if (type == null) {
                LoggerUtil.warn("Received message without 'type' field: " + message);
                return;
            }

            Class<? extends InboundPacket> packetClass = inboundPacketTypes.get(type);
            if (packetClass != null) {
                InboundPacket packet = null;
                try {
                    if (payloadObj instanceof Map) {
                        String payloadJson = gson.toJson(payloadObj);
                        packet = gson.fromJson(payloadJson, packetClass);
                    } else {
                        packet = gson.fromJson(message, packetClass);
                    }
                } catch (Exception e) {
                    LoggerUtil.error("Failed to deserialize message into " + packetClass.getSimpleName() + ": " + message);
                }

                if (packet != null) {
                    packet.handle();
                }
            } else {
                LoggerUtil.warn("Unknown message type received: " + type);
            }
        } catch (JsonSyntaxException e) {
            LoggerUtil.error("Failed to parse JSON message: " + message, e);
        } catch (Exception e) {
            LoggerUtil.error("Error processing WebSocket message: " + message, e);
        }
    }


    @Override
    public void onClose(int code, String reason, boolean remote) {
        isOpen = false;
        LoggerUtil.warn("Disconnected from WebSocket server. Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
        reconnectAttempts++;
        long delay = Math.min(5L * reconnectAttempts, 60L);
        reconnectionScheduler.schedule(() -> {
            LoggerUtil.info("Attempting to reconnect to WebSocket server (attempt " + reconnectAttempts + ")...");
            try {
                reconnect();
            } catch (Exception e) {
                LoggerUtil.error("WebSocket reconnect error: " + e.getMessage(), e);
            }
        }, delay, TimeUnit.SECONDS);
    }

    @Override
    public void onError(Exception ex) {
        LoggerUtil.error("WebSocket error occurred: " + ex.getMessage(), ex);
        isOpen = false;
    }

    public void sendPacket(OutboundPacket packet) {
        if (isOpen()) {
            String jsonMessage = gson.toJson(packet);
            LoggerUtil.debug("Sending message: " + jsonMessage);
            send(jsonMessage);
        } else {
            LoggerUtil.warn("WebSocket not open. Cannot send packet: " + packet.getType());
        }
    }
    public boolean isOpen() {
        return isOpen;
    }


    public void stopBankUpdateTask() {
        bankUpdateScheduler.shutdownNow();
    }

}