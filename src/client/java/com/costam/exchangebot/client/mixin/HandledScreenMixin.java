package com.costam.exchangebot.client.mixin;

import com.costam.exchangebot.client.event.GuiEventHandler;
import com.costam.exchangebot.client.event.InventoryEventHandler;
import com.costam.exchangebot.client.util.TransactionUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    private static final Pattern TRADE_GUI_PATTERN = Pattern.compile(":\\|:\\s*([A-Za-z0-9_\\-]+)");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\$([0-9]+)");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("Wytworzył[: ]+([A-Za-z0-9_]+)");

    
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int PLAYER_INVENTORY_HEIGHT = 94;


    @Inject(method = "removed", at = @At("HEAD"))
    private void onHandledScreenClosed(CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        Text title = screen.getTitle();
        String titleString = title.getString();

        Matcher matcher = TRADE_GUI_PATTERN.matcher(titleString);
        if (matcher.find()) {
            String matchedName = matcher.group(1);
            ItemStack stack45 = screen.getScreenHandler().getSlot(45).getStack();

            if (matchedName.equals(TransactionUtil.getLastTransactionPlayerName())
                    && !stack45.isEmpty() && stack45.getItem() == Items.LIME_DYE) {

                TransactionUtil.setLastTransactionBase64(takeGuiScreenshotBase64(screen));
                scheduler.schedule(() -> {
                    MinecraftClient.getInstance().execute(() -> {
                        InventoryEventHandler.setIsActive(true);
                    });
                }, 1, TimeUnit.SECONDS);
            }else if(matchedName.equals(TransactionUtil.getLastTransactionPlayerName())){

                TransactionUtil.reset();
                GuiEventHandler.setRunning(false);
                GuiEventHandler.setCurrentSlot(0);
                InventoryEventHandler.setRunning(true);
            }
        }
    }
     

    private static String takeGuiScreenshotBase64(HandledScreen<?> screen) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!client.isOnThread()) {
            final String[] result = new String[1];
            client.execute(() -> result[0] = takeGuiScreenshotBase64(screen));
            return result[0];
        }

        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        int fullGuiWidth = accessor.getBackgroundWidth();
        int fullGuiHeight = accessor.getBackgroundHeight();

        int guiHeight = fullGuiHeight - PLAYER_INVENTORY_HEIGHT;
        int guiWidth = fullGuiWidth;

        int guiLeft = (screen.width - guiWidth) / 2;
        int guiTop = (screen.height - fullGuiHeight) / 2;

        Window window = client.getWindow();
        double scale = window.getScaleFactor();

        int fbGuiLeft = (int) Math.round(guiLeft * scale);
        int fbGuiTop = (int) Math.round(guiTop * scale);

        int fbGuiWidth = (int) Math.round(guiWidth * scale);
        int fbGuiHeight = (int) Math.round(guiHeight * scale);

        int fbWidth = window.getFramebufferWidth();
        int fbHeight = window.getFramebufferHeight();

        fbGuiWidth = Math.min(fbGuiWidth, fbWidth - fbGuiLeft);
        fbGuiHeight = Math.min(fbGuiHeight, fbHeight - fbGuiTop);

        if (fbGuiWidth <= 0 || fbGuiHeight <= 0) {
            return null;
        }

        IntBuffer buffer = BufferUtils.createIntBuffer(fbWidth * fbHeight);
        GL11.glReadPixels(0, 0, fbWidth, fbHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

        int[] pixels = new int[fbWidth * fbHeight];
        buffer.get(pixels);

        BufferedImage fullScreen = new BufferedImage(fbWidth, fbHeight, BufferedImage.TYPE_INT_ARGB);
        fullScreen.setRGB(0, 0, fbWidth, fbHeight, pixels, 0, fbWidth);

        BufferedImage scaledGuiImage = new BufferedImage(fbGuiWidth, fbGuiHeight, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < fbGuiHeight; y++) {
            for (int x = 0; x < fbGuiWidth; x++) {
                int pixel = fullScreen.getRGB(fbGuiLeft + x, fbHeight - (fbGuiTop + y) - 1);
                scaledGuiImage.setRGB(x, y, pixel);
            }
        }

        BufferedImage finalGuiImage = new BufferedImage(guiWidth, guiHeight, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = finalGuiImage.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(scaledGuiImage, 0, 0, guiWidth, guiHeight, null);
        g2d.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalGuiImage, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




}

