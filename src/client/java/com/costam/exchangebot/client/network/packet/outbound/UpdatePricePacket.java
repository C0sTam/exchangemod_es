package com.costam.exchangebot.client.network.packet.outbound;

public class UpdatePricePacket implements OutboundPacket {
    private final String type = "UPDATE_ITEM_PRICE";
    private final Payload payload;

    public UpdatePricePacket(int itemId, Integer lowestPrice, Integer highestPrice, Double averagePrice, Long totalPrice, Integer itemCount) {
        this.payload = new Payload(itemId, lowestPrice, highestPrice, averagePrice, totalPrice, itemCount);
    }

    @Override
    public String getType() {
        return type;
    }

    private static class Payload {
        public int itemId;
        public Integer lowestPrice;
        public Integer highestPrice;
        public Double averagePrice;
        public Long totalPrice;
        public Integer itemCount;

        public Payload(int itemId, Integer lowestPrice, Integer highestPrice, Double averagePrice, Long totalPrice, Integer itemCount) {
            this.itemId = itemId;
            this.lowestPrice = lowestPrice;
            this.highestPrice = highestPrice;
            this.averagePrice = averagePrice;
            this.totalPrice = totalPrice;
            this.itemCount = itemCount;
        }
    }
}
