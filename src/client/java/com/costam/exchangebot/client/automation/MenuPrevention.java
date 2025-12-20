package com.costam.exchangebot.client.automation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Class to track state and implement menu prevention that allows for replays to work in background
 */
public class MenuPrevention {

    // Default to true as requested
    public static boolean preventToBackground = true;
    public static boolean registered = false;

    private MenuPrevention() {
        // Private constructor to prevent instantiation
    }

    public static void register() {
        registered = true;
    }

    public static void onTick(MinecraftClient client) {
        if (!registered || client.player == null) return;

        // Enforce cursor state every tick if active
        if (preventToBackground && client.currentScreen == null) {
             if (client.mouse.isCursorLocked()) {
                 client.mouse.unlockCursor();
             }
        }
    }

    public static void renderIcon(DrawContext context) {
        // Rendering the text to show that menu opening is blocked and the mouse can be used freely
        if (preventToBackground) {
            MinecraftClient client = MinecraftClient.getInstance();
            // Fallback text rendering
            String text = "MOUSE UNLOCKED (BG MODE) (key: P)";
            int width = client.textRenderer.getWidth(text);
            int x = (client.getWindow().getScaledWidth() - width) / 2;
            int y = client.getWindow().getScaledHeight() / 2 + 10;
            context.drawText(client.textRenderer, text, x, y, 0xFF5555, true);
        }
    }

    public static void toggleBackgroundPrevention() {
        if (!registered) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Do not allow background prevention to be toggled while a screen is opened
        if (client.currentScreen != null) {
            return;
        }

        preventToBackground = !preventToBackground;

        client.player.sendMessage(Text.translatable("Menu Prevention: ").append(preventToBackground ? ScreenTexts.ON : ScreenTexts.OFF), true);

        if (preventToBackground) {
            client.mouse.unlockCursor();
        } else {
            client.mouse.lockCursor();
        }
    }
}