package com.costam.exchangebot.client.models;

public class Item {
    private int id;  // Zmienione na int, bo w JSON jest liczba
    private String material;
    private String name;
    private String lore;
    private Integer customModelData;
    private Integer lowestPrice = null;
    private Integer highestPrice = null;
    private Long totalPrice = null;
    private Integer itemCount = 0;

    // Gettery
    public int getId() {
        return id;
    }

    public String getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public String getLore() {
        return lore;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public Integer getLowestPrice() {
        return lowestPrice;
    }

    public Integer getHighestPrice() {
        return highestPrice;
    }


    public Long getTotalPrice() {
        return totalPrice;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    // Settery (potrzebne dla deserializacji JSON)
    public void setId(int id) {
        this.id = id;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public void setCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
    }

    public void setLowestPrice(Integer lowerPrice) {
        this.lowestPrice = lowerPrice;
    }

    public void setHighestPrice(Integer highestPrice) {
        this.highestPrice = highestPrice;
    }


    public void setTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
}