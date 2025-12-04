package com.costam.exchangebot.client.models;

public class Item {
    private int id;  // Zmienione na int, bo w JSON jest liczba
    private String material;
    private String name;
    private String lore;
    private Integer customModelData;

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
}