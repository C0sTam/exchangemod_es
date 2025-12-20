package com.costam.exchangebot.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

public class ServerInfoUtil {



    private static String serverBrand = null;
    private static net.minecraft.client.network.ServerInfo lastServerInfo = null;
    private static volatile String preferredAccountMode = null;
    private static volatile String desiredSpawnChannel = "SPAWN01";
    private static volatile long lifestealEnterAtMs = 0L;



    public static String getServerBrand() {
        return serverBrand;
    }
    public static void setServerBrand(String serverBrand) {
        ServerInfoUtil.serverBrand = serverBrand;
    }
    public static String getServerType() {
        if (serverBrand == null) return null;

        String brandLower = serverBrand.trim().toLowerCase();

        if (isBoxPvPShardPresentOnScoreboard()) return "BOXPVP";

        if (brandLower.startsWith("hyperion")) {
            if (isLobbyWaitingRoomsPresentOnScoreboard()) return "LOBBY";
            if (isMoneyLabelPresentOnScoreboard()) return "LIFESTEAL";
            return "LOBBY";
        }

        String brandUpper = brandLower.toUpperCase();
        if (brandUpper.startsWith("LOBBY")||brandUpper.startsWith("S")) return "LOBBY";
        if (brandUpper.startsWith("SPAWN")) return "LIFESTEAL";

        return null;
    }

    private static boolean isLobbyWaitingRoomsPresentOnScoreboard() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null) return false;
            Scoreboard scoreboard = client.world.getScoreboard();
            if (scoreboard == null) return false;

            for (Team team : scoreboard.getTeams()) {
                String prefix = team.getPrefix() != null ? team.getPrefix().getString() : "";
                String suffix = team.getSuffix() != null ? team.getSuffix().getString() : "";
                String text = (prefix + suffix).replaceAll("§[0-9a-fk-or]", "").toLowerCase();
                if (text.contains("osób w poczekalniach") || text.contains("poczekalniach") || text.contains("poczekalnia")) {
                    return true;
                }
            }
        } catch (Exception ignored) { }
        return false;
    }

    private static boolean isMoneyLabelPresentOnScoreboard() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null) return false;
            Scoreboard scoreboard = client.world.getScoreboard();
            if (scoreboard == null) return false;

            for (Team team : scoreboard.getTeams()) {
                String prefix = team.getPrefix() != null ? team.getPrefix().getString() : "";
                String suffix = team.getSuffix() != null ? team.getSuffix().getString() : "";
                String text = (prefix + suffix).replaceAll("§[0-9a-fk-or]", "").toLowerCase();
                if (text.contains("pieniądze")) {
                    return true;
                }
            }
        } catch (Exception ignored) { }
        return false;
    }

    private static boolean isBoxPvPShardPresentOnScoreboard() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null) return false;
            Scoreboard scoreboard = client.world.getScoreboard();
            if (scoreboard == null) return false;

            for (Team team : scoreboard.getTeams()) {
                String prefix = team.getPrefix() != null ? team.getPrefix().getString() : "";
                String suffix = team.getSuffix() != null ? team.getSuffix().getString() : "";
                String text = (prefix + suffix).replaceAll("§[0-9a-fk-or]", "");
                String textLower = text.toLowerCase();
                if (textLower.contains("odłamek") || textLower.contains("odlamek")) {
                    return true;
                }
            }
        } catch (Exception ignored) { }
        return false;
    }

    public static String getServerAddress() {
        if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
            return MinecraftClient.getInstance().getCurrentServerEntry().address;
        }
        return "singleplayer";
    }

    public static void captureCurrentServer() {
        if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
            lastServerInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        }
    }

    public static net.minecraft.client.network.ServerInfo getLastServerInfo() {
        return lastServerInfo;
    }

    public static void setPreferredModeForUsername(String username) {
        if (username == null) { preferredAccountMode = null; return; }
        String u = username.trim();
        if (u.equalsIgnoreCase("Micho1")) preferredAccountMode = "LIFESTEAL";
        else if (u.equalsIgnoreCase("Matejson")) preferredAccountMode = "BOXPVP";
        else preferredAccountMode = null;
    }

    public static String getPreferredAccountMode() {
        return preferredAccountMode;
    }

    public static String getDesiredSpawnChannel() { return desiredSpawnChannel; }
    public static void setDesiredSpawnChannel(String channel) {
        if (channel == null) desiredSpawnChannel = "SPAWN01";
        else desiredSpawnChannel = channel.toUpperCase().contains("SPAWN02") ? "SPAWN02" : "SPAWN01";
    }
    public static void markLifestealEnter() { lifestealEnterAtMs = System.currentTimeMillis(); }
    public static void resetLifestealEnter() { lifestealEnterAtMs = 0L; }
    public static boolean isLifestealInCooldown() {
        String mode = getServerType();
        return "LIFESTEAL".equals(mode) && lifestealEnterAtMs != 0L && (System.currentTimeMillis() - lifestealEnterAtMs) < 60_000L;
    }

    public static boolean isSpawn01ChannelOnScoreboard() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null) return false;
            Scoreboard scoreboard = client.world.getScoreboard();
            if (scoreboard == null) return false;
            String needle = "jesteś na spawn02 (/ch)";
            for (Team team : scoreboard.getTeams()) {
                String prefix = team.getPrefix() != null ? team.getPrefix().getString() : "";
                String suffix = team.getSuffix() != null ? team.getSuffix().getString() : "";
                String text = (prefix + suffix).replaceAll("§[0-9a-fk-or]", "").toLowerCase();
                if (text.contains(needle)) return true;
            }
        } catch (Exception ignored) { }
        return false;
    }
}

