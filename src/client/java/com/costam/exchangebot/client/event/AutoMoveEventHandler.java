package com.costam.exchangebot.client.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.costam.exchangebot.client.util.ServerInfoUtil;

public class AutoMoveEventHandler {

    private static int tickCounter = 0; 
    private static int moveDuration = 0; 
    private static final int ticksPerMinute = 20 * 60; 
    private static final int ticksMoveTime = 20 * 5;  
    private static boolean moving = false;
    private static boolean moveLeft = true;
    private static long lifestealEnterAtMs = 0L;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (client.player != null && client.currentScreen == null) {
                String mode = ServerInfoUtil.getServerType();
                long nowMs = System.currentTimeMillis();
                if ("LOBBY".equals(mode)) {
                    moving = false;
                    client.options.leftKey.setPressed(false);
                    client.options.rightKey.setPressed(false);
                    lifestealEnterAtMs = 0L;
                    return;
                }
                if ("BOXPVP".equals(mode)) {
                    moving = false;
                    client.options.leftKey.setPressed(false);
                    client.options.rightKey.setPressed(false);
                    return;
                }
                if ("LIFESTEAL".equals(mode)) {
                    if (lifestealEnterAtMs == 0L) lifestealEnterAtMs = nowMs;
                    if (nowMs - lifestealEnterAtMs < 60_000L) {
                        moving = false;
                        client.options.leftKey.setPressed(false);
                        client.options.rightKey.setPressed(false);
                        return;
                    }
                } else {
                    lifestealEnterAtMs = 0L;
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
}
