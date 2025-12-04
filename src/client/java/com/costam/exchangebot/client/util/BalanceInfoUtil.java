package com.costam.exchangebot.client.util;

import com.costam.exchangebot.client.ExchangebotClient;
import com.costam.exchangebot.client.network.packet.outbound.BankUpdatePacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.scoreboard.*;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalanceInfoUtil {
    private static final ScheduledExecutorService bankUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
    private static Double balance = 0.0;
    private static double lastMoneyAmount = 0.0;
    private static final Pattern MONEY_PATTERN = Pattern.compile("Pieniądze:\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(k|MLN|mld)?\\$?", Pattern.CASE_INSENSITIVE);

    public static void startBankUpdateTask() {
        bankUpdateScheduler.scheduleAtFixedRate(() -> {
            BalanceInfoUtil.checkScoreboard(MinecraftClient.getInstance());
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void stopBankUpdateTask() {
        bankUpdateScheduler.shutdownNow();
    }

    public static void checkBalance() {
        // celowo puste: nie wysyłamy już komendy "/money"
    }

    public static Double getBalance() {
        return balance;
    }

    public static void setBalance(Double balance) {
        BalanceInfoUtil.balance = balance;
    }

    private static void checkScoreboard(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) {
            return;
        }

        double money = 0.0;
        for (Team team : scoreboard.getTeams()) {
            String prefix = team.getPrefix() != null ? team.getPrefix().getString() : "";
            String suffix = team.getSuffix() != null ? team.getSuffix().getString() : "";
            double parsed = extractMoneyFromText(prefix + suffix);
            if (parsed > 0) {
                money = parsed;
                break;
            }
        }

        if (money > 0) {
            updateMoney(money);
        } else {
            LoggerUtil.debug("Nie znaleziono salda na scoreboardzie.");
        }
    }
    private static double extractMoneyFromText(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        String cleanText = text.replaceAll("§[0-9a-fk-or]", "");
        Matcher matcher = MONEY_PATTERN.matcher(cleanText);
        if (matcher.find()) {
            double amount = parseDouble(matcher.group(1));
            String unit = matcher.group(2);
            return normalizeAmount(amount, unit);
        }
        return 0.0;
    }

    private static double parseDouble(String str) {
        try {

            str = str.replace(",", ".");
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static void updateMoney(double newAmount) {
        if (newAmount != lastMoneyAmount) {
            lastMoneyAmount = newAmount;
            balance = newAmount;
            LoggerUtil.info("Wykryto pieniądze: " + newAmount);

            if (ExchangebotClient.getWebSocketClient().isOpen()) {
                LoggerUtil.info("Bank update sent: " + balance);
                ExchangebotClient.getWebSocketClient().sendPacket(new BankUpdatePacket(balance));
            } else {
                LoggerUtil.warn("WebSocket not open. Skipping bank update.");
            }
        }
    }
    private static double normalizeAmount(double amount, String unit) {
        if (unit == null) return amount;
        switch (unit.toLowerCase()) {
            case "k":   return amount * 1_000;
            case "mln": return amount * 1_000_000;
            case "mld": return amount * 1_000_000_000;
            default:    return amount;
        }
    }
}