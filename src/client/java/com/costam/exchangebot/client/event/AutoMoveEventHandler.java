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

    public static void disableMovementFor(long durationMs) {
        movementDisabledUntil = System.currentTimeMillis() + durationMs;
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
                if ("LOBBY".equalsIgnoreCase(mode)) {
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
}
