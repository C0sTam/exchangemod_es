package com.costam.exchangebot.client.network.packet.inbound;

import com.costam.exchangebot.client.models.Item;
import com.costam.exchangebot.client.util.LoggerUtil;
import com.costam.exchangebot.client.util.TransactionUtil;

public class ItemsResponsePacket implements InboundPacket {
    private Item[] items;

    public Item[] getItems() {
        return items;
    }

    @Override
    public void handle() {
        LoggerUtil.info("Received ITEMS from server: " + (items != null ? items.length : 0) + " items");

        if (items == null) {
            LoggerUtil.warn("Received null items array");
            return;
        }

        TransactionUtil.clear();

        for (Item item : items) {
            TransactionUtil.add(item);
            // Logowanie szczegółów każdego itemu
            LoggerUtil.info("Item: id=" + item.getId() +
                    ", material=" + item.getMaterial() +
                    ", name=" + item.getName() +
                    ", lore=" + item.getLore() +
                    ", customModelData=" + item.getCustomModelData());
        }

        LoggerUtil.info("Added " + items.length + " items to TransactionUtil");
    }
}