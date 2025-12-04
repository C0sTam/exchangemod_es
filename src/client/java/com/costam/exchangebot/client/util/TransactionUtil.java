package com.costam.exchangebot.client.util;

import com.costam.exchangebot.client.models.Item;

import java.util.Arrays;

public class TransactionUtil {

    private static Integer lastTransactionId;
    private static String lastTransactionPlayerName = null;
    private static Integer lastTransactionAmount = null;

    private static String lastTransactionBase64 = null;
    private static Item[] items;

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
    public Item[] getItems() {
        return items;
    }

    public static void add(Item item) {
        if (item == null) return;
        items = Arrays.copyOf(items, items.length + 1);
        items[items.length - 1] = item;
    }

    public void remove(int index) {
        if (index < 0 || index >= items.length) return;
        Item[] newItems = new Item[items.length - 1];
        System.arraycopy(items, 0, newItems, 0, index);
        System.arraycopy(items, index + 1, newItems, index, items.length - index - 1);
        items = newItems;
    }

    public static void clear() {
        items = new Item[0];
    }
}
