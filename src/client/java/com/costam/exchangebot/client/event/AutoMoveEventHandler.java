package com.costam.exchangebot.client.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.costam.exchangebot.client.util.ServerInfoUtil;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoMoveEventHandler {

    private static int tickCounter = 0; 
    private static int moveDuration = 0; 
    private static final int ticksPerMinute = 20 * 60; 
    private static final int ticksMoveTime = 20 * 5;  
    private static boolean moving = false;
    private static boolean moveLeft = true;
    private static long lastLobbyUseMs = 0L;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static volatile boolean lobbyRetrySlowMode = false;
    private static long movementDisabledUntil = 0L;
    private static volatile boolean waitingForVerified = false;
    private static volatile boolean waitingForGui = false;
    private static String lastMode = null;
    private static boolean lobbyMoveActive = false;
    private static int lobbyMoveTicks = 0;

    public static void disableMovementFor(long durationMs) {
        movementDisabledUntil = System.currentTimeMillis() + durationMs;
    }

    public static boolean isMovementDisabled() {
        return System.currentTimeMillis() < movementDisabledUntil;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (client.player != null && client.currentScreen == null) {
                long nowMs = System.currentTimeMillis();

                if (nowMs < movementDisabledUntil) {
                    moving = false;
                    client.options.leftKey.setPressed(false);
                    client.options.rightKey.setPressed(false);
                    return;
                }

                String mode = ServerInfoUtil.getServerType();
                if (mode != null && (lastMode == null || !mode.equalsIgnoreCase(lastMode))) {
                    lastMode = mode;
                    if ("LOBBY".equalsIgnoreCase(mode)) {
                        lobbyMoveActive = true;
                        lobbyMoveTicks = 0;
                    } else {
                        lobbyMoveActive = false;
                        lobbyMoveTicks = 0;
                    }
                }
                if ("LOBBY".equalsIgnoreCase(mode)) {
                    if (lobbyMoveActive) {
                        lobbyMoveTicks++;
                        client.options.forwardKey.setPressed(true);
                        client.player.setYaw(client.player.getYaw() + 1.0F);
                        client.player.setPitch(client.player.getPitch() + 0.5F);
                        if (lobbyMoveTicks >= 40) {
                            lobbyMoveActive = false;
                            lobbyMoveTicks = 0;
                            client.options.forwardKey.setPressed(false);
                            waitingForVerified = true;
                        }
                    } else {
                        client.options.forwardKey.setPressed(false);
                    }

                    moving = false;
                    client.options.leftKey.setPressed(false);
                    client.options.rightKey.setPressed(false);
                    return;
                }

                if ("BOXPVP".equalsIgnoreCase(mode)) {
                    moving = false;
                    tickCounter = 0;
                    moveDuration = 0;
                    client.options.leftKey.setPressed(false);
                    client.options.rightKey.setPressed(false);
                    return;
                }
                
                if (!moving && tickCounter >= ticksPerMinute) {

                    tickCounter = 0;
                    moving = true;
                    moveLeft = !moveLeft; 
                    moveDuration = 0;
                }

                if (moving) {
                    
                    moveDuration++;
                    if (moveLeft) {
                        client.options.leftKey.setPressed(true);
                        client.options.rightKey.setPressed(false);
                    } else {
                        client.options.leftKey.setPressed(false);
                        client.options.rightKey.setPressed(true);
                    }

                    if (moveDuration >= ticksMoveTime) {
                        
                        moving = false;
                        client.options.leftKey.setPressed(false);
                        client.options.rightKey.setPressed(false);
                    }
                }
            }
        });
    }
    private static void onTick() {

    }
    public static void setLobbyRetrySlowMode(boolean slow) { lobbyRetrySlowMode = slow; }
    public static void setWaitingForVerified(boolean v) { waitingForVerified = v; }
    public static boolean isWaitingForVerified() { return waitingForVerified; }
    public static void setWaitingForGui(boolean g) { waitingForGui = g; }
    public static boolean isWaitingForGui() { return waitingForGui; }
}
