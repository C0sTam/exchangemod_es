package com.costam.exchangebot.client.util;

public class TransactionUtil {

    private static Integer lastTransactionId;
    private static String lastTransactionPlayerName = null;
    private static Integer lastTransactionAmount = null;

    private static String lastTransactionBase64 = null;

    public static Integer getLastTransactionId() {
        return lastTransactionId;
    }

    public static void setLastTransactionId(Integer id) {
        lastTransactionId = id;
    }

    public static String getLastTransactionPlayerName() {
        return lastTransactionPlayerName;
    }

    public static void setLastTransactionPlayerName(String playerName) {
        lastTransactionPlayerName = playerName;
    }
    public static Integer getLastTransactionAmount() {
        return lastTransactionAmount;
    }
    public static void setLastTransactionAmount(Integer lastTransactionAmount) {
        TransactionUtil.lastTransactionAmount = lastTransactionAmount;
    }
    public static String getLastTransactionBase64() {
        return lastTransactionBase64;
    }

    public static void setLastTransactionBase64(String lastTransactionBase64) {
        TransactionUtil.lastTransactionBase64 = lastTransactionBase64;
    }

    public static void reset() {
        lastTransactionId = null;
        lastTransactionPlayerName = null;
        lastTransactionAmount = null;
        lastTransactionBase64 = null;
    }
}
