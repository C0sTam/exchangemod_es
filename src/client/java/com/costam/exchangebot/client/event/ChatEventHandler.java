package com.costam.exchangebot.client.event;

import com.costam.exchangebot.client.ExchangebotClient;
import com.costam.exchangebot.client.network.packet.outbound.BankUpdatePacket;
import com.costam.exchangebot.client.network.packet.outbound.TransactionCompletedOutboundPacket;
import com.costam.exchangebot.client.network.packet.outbound.TransactionCreatePacket;
import com.costam.exchangebot.client.network.packet.outbound.TransactionRequestPacket;
import com.costam.exchangebot.client.network.packet.outbound.FullscreenPacket;
import com.costam.exchangebot.client.network.packet.outbound.VerifyAccountPacket;
import com.costam.exchangebot.client.util.*;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEventHandler {

    private static final Pattern BALANCE_PATTERN = Pattern.compile("^Stan twojego konta wynosi ([0-9]+(?:\\.[0-9]{1,2})?)\\$.*");
    private static final Pattern RECEIVE_PATTERN = Pattern.compile("^Otrzymałeś ([0-9]+(?:[.,][0-9]{1,2})?(?:k|MLN|mld)?)\\$ od ([^\\s!]+)!.*");
    private static final Pattern SENT_PATTERN = Pattern.compile("^Wysłano ([0-9]+(?:[.,][0-9]{1,2})?(?:k|MLN|mld)?)\\$ dla (\\S+).*");
    private static final Pattern TRADE_REQUEST_PATTERN = Pattern.compile("^Otrzymałeś prośbę o handel od gracza (\\S+)!.*");
    private static final Pattern PLAYER_NOT_AVAILABLE_PATTERN = Pattern.compile(
            "Gracz znajduję się zbyt daleko od Ciebie.*|Taki gracz nie jest aktualnie na twoim sektorze.*|not found message commands.player.trade.too.far.*|commands.player.trade.too.far.*|not found message commands.player.trade.player.not.found.*|commands.player.trade.player.not.found.*|Nastepna komende możesz wpisać na.*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern VERIFY_PATTERN = Pattern.compile("^\\[(\\S+) -> Ja\\] kod weryfikacyjny: ([A-Za-z0-9]{6}).*");

    static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//



    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String raw = ColorStripUtils.stripAllColorsAndFormats(message.getString());
            if (raw.contains("Zweryfikowano czynnik zaufania") && raw.contains("Możesz kontynuować grę")) {
                if (!"LOBBY".equals(ServerInfoUtil.getServerType())) {
                    scheduler.schedule(() -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client != null) {
                            client.execute(() -> {
                                client.options.useKey.setPressed(true);
                            });
                            scheduler.schedule(() -> {
                                MinecraftClient c2 = MinecraftClient.getInstance();
                                if (c2 != null) c2.execute(() -> c2.options.useKey.setPressed(false));
                            }, 300, TimeUnit.MILLISECONDS);
                        }
                    }, 200, TimeUnit.MILLISECONDS);
                }
            }
            Matcher balanceMatcher = BALANCE_PATTERN.matcher(raw);

            if (balanceMatcher.find()) {
                double balance = Double.parseDouble(balanceMatcher.group(1));
                if(balance != BalanceInfoUtil.getBalance()) {
                    if (ExchangebotClient.getWebSocketClient().isOpen()) {
                        LoggerUtil.info("Bank update sent: " + balance);
                        ExchangebotClient.getWebSocketClient().sendPacket(new BankUpdatePacket(balance));
                    } else {
                        LoggerUtil.warn("WebSocket not open. Skipping bank update.");
                    }
                    BalanceInfoUtil.setBalance(Double.parseDouble(balanceMatcher.group(1)));
                } else {
                    LoggerUtil.debug("Balance remains unchanged: " + balance);
                }
            }
            Matcher verifyMatcher = VERIFY_PATTERN.matcher(raw);
            if (verifyMatcher.find()) {
                String sender = verifyMatcher.group(1);
                String code = verifyMatcher.group(2);

                LoggerUtil.info(String.format("Received verification code: sender='%s', code='%s'", sender, code));

                if (ExchangebotClient.getWebSocketClient().isOpen()) {
                    LoggerUtil.info(String.format("Sending verification packet: minecraftNick='%s', code='%s'", sender, code));
                    ExchangebotClient.getWebSocketClient().sendPacket(new VerifyAccountPacket(sender, code));
                } else {
                    LoggerUtil.warn("WebSocket connection is closed. Verification packet skipped.");
                }
            }
            Matcher receiveMatcher = RECEIVE_PATTERN.matcher(raw);
            if (receiveMatcher.find()) {
                if (ServerInfoUtil.isLifestealInCooldown()) return;
                String sender = receiveMatcher.group(2);
                Double amount = PriceFormatter.parsePrice(receiveMatcher.group(1));

                LoggerUtil.info(String.format("Received transaction create request: sender='%s', amount=%.2f", sender, amount));

                if (ExchangebotClient.getWebSocketClient().isOpen()) {
                    LoggerUtil.info(String.format("Sending transaction create: sender='%s', amount=%.2f", sender, amount));
                    ExchangebotClient.getWebSocketClient().sendPacket(new TransactionCreatePacket(sender, amount));
                } else {
                    LoggerUtil.warn("WebSocket connection is closed. Transaction create skipped.");
                }
            }
            Matcher sentMatcher = SENT_PATTERN.matcher(raw);
            if (sentMatcher.find()) {
                if (ServerInfoUtil.isLifestealInCooldown()) return;
                String sender = sentMatcher.group(2);
                Double amount = PriceFormatter.parsePrice(sentMatcher.group(1));

                scheduler.schedule(() -> {
                    MinecraftClient client = MinecraftClient.getInstance();

                    
                    if (client == null || client.getWindow() == null || client.inGameHud == null) {
                        LoggerUtil.info(String.format("Client null, sending transaction without image: sender='%s', amount=%.2f", sender, amount));

                        if (ExchangebotClient.getWebSocketClient().isOpen()) {
                            if (TransactionUtil.getLastTransactionId() != null) {
                                ExchangebotClient.getWebSocketClient().sendPacket(
                                        new TransactionCompletedOutboundPacket(TransactionUtil.getLastTransactionId(), amount, sender, null)
                                );
                                TransactionUtil.reset();
                            }
                        } else {
                            LoggerUtil.warn("WebSocket connection is closed. Transaction create skipped.");
                        }
                        return; 
                    }

                    client.execute(() -> {
                        Window window = client.getWindow();
                        int fbWidth = window.getFramebufferWidth();
                        int fbHeight = window.getFramebufferHeight();

                        IntBuffer buffer = ByteBuffer
                                .allocateDirect(fbWidth * fbHeight * 4)
                                .order(ByteOrder.nativeOrder())
                                .asIntBuffer();

                        GL11.glReadBuffer(GL11.GL_FRONT);
                        GL11.glReadPixels(0, 0, fbWidth, fbHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                        int[] pixels = new int[fbWidth * fbHeight];
                        buffer.get(pixels);

                        BufferedImage full = new BufferedImage(fbWidth, fbHeight, BufferedImage.TYPE_INT_ARGB);
                        for (int row = 0; row < fbHeight; row++) {
                            for (int col = 0; col < fbWidth; col++) {
                                int i = row * fbWidth + col;
                                int pixel = pixels[i];
                                int r = pixel & 0xFF;
                                int g = (pixel >> 8) & 0xFF;
                                int b = (pixel >> 16) & 0xFF;
                                int a = (pixel >> 24) & 0xFF;
                                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                                full.setRGB(col, fbHeight - row - 1, argb);
                            }
                        }

                        
                        ChatHud chat = client.inGameHud.getChatHud();
                        double scale = window.getScaleFactor();

                        int chatWidth = (int) (chat.getWidth() * scale);
                        int chatHeight = (int) (chat.getHeight() * scale);
                        int chatX = (int) (2 * scale);
                        int chatY = fbHeight - chatHeight - (int) (40 * scale);

                        int bottomHeight = (int) (chatHeight * 0.25);
                        int bottomY = chatY + chatHeight - bottomHeight;

                        if (chatX < 0) chatX = 0;
                        if (bottomY < 0) bottomY = 0;
                        if (chatX + chatWidth > fbWidth) chatWidth = fbWidth - chatX;
                        if (bottomY + bottomHeight > fbHeight) bottomHeight = fbHeight - bottomY;

                        BufferedImage cropped = full.getSubimage(chatX, bottomY, chatWidth, bottomHeight);

                        
                        new Thread(() -> {
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                ImageIO.write(cropped, "png", baos);
                                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                                LoggerUtil.info(String.format("Received sent request: sender='%s', amount=%.2f", sender, amount));

                                if (ExchangebotClient.getWebSocketClient().isOpen()) {
                                    LoggerUtil.info(String.format("Sending sent : sender='%s', amount=%.2f", sender, amount));
                                    if (TransactionUtil.getLastTransactionId() != null) {
                                        ExchangebotClient.getWebSocketClient().sendPacket(
                                                new TransactionCompletedOutboundPacket(TransactionUtil.getLastTransactionId(), amount, sender, base64)
                                        );
                                        try (ByteArrayOutputStream fbaos = new ByteArrayOutputStream()) {
                                            ImageIO.write(full, "png", fbaos);
                                            String fullBase64 = Base64.getEncoder().encodeToString(fbaos.toByteArray());
                                            ExchangebotClient.getWebSocketClient().sendPacket(
                                                    new FullscreenPacket(TransactionUtil.getLastTransactionId(), amount, sender, new String[]{fullBase64, fullBase64})
                                            );
                                        } catch (IOException ignored) { }
                                        TransactionUtil.reset();
                                    }
                                } else {
                                    LoggerUtil.warn("WebSocket connection is closed. Transaction create skipped.");
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                }, 200, TimeUnit.MILLISECONDS);
            }

            Matcher tradeMatcher = TRADE_REQUEST_PATTERN.matcher(raw);
            if (tradeMatcher.find()) {
                if (ServerInfoUtil.isLifestealInCooldown()) return;
                String sender = tradeMatcher.group(1);

                LoggerUtil.info(String.format("Received trade request: sender='%s'", sender));
                if(InventoryEventHandler.isBlocked()|| MinecraftClient.getInstance().currentScreen != null) return;
                InventoryEventHandler.setBlocked(true);
                TransactionUtil.setPendingStatsPlayerName(sender);
                TransactionUtil.setWaitingStatsConfirmation(true);

                long nowMs = System.currentTimeMillis();
                long last = TransactionUtil.getLastStatsCommandAtMs();
                long delay = Math.max(0, 1000 - (nowMs - last));
                scheduler.schedule(() -> {
                    MinecraftClient c = MinecraftClient.getInstance();
                    if (c != null && c.player != null && c.player.networkHandler != null) {
                        c.player.networkHandler.sendChatCommand("stats " + sender);
                        TransactionUtil.setLastStatsCommandAtMs(System.currentTimeMillis());
                        LoggerUtil.info(String.format("Sent /stats for '%s'", sender));
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
            Matcher notAvailblePattern = PLAYER_NOT_AVAILABLE_PATTERN.matcher(raw);
            if (notAvailblePattern.find()) {

                if(!InventoryEventHandler.isBlocked()) {
                    InventoryEventHandler.setBlocked(true);
                    LoggerUtil.info("Player not available, blocking inventory.");
                }
                InventoryEventHandler.setRunning(true);
                TransactionUtil.reset();
            }

            if (raw.contains("Serwer jest niedostępny! Brak aktywnego kanału")) {
                AutoMoveEventHandler.setLobbyRetrySlowMode(true);
            }

        });
    }
}
