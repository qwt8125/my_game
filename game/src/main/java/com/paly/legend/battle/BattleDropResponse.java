package com.paly.legend.battle;

public class BattleDropResponse {

    private long inventoryItemId;
    private String itemId;
    private String name;
    private String type;
    private String slot;
    private String quality;
    private int quantity;

    public BattleDropResponse() {
    }

    public BattleDropResponse(long inventoryItemId, String itemId, String name, String type,
                              String slot, String quality, int quantity) {
        this.inventoryItemId = inventoryItemId;
        this.itemId = itemId;
        this.name = name;
        this.type = type;
        this.slot = slot;
        this.quality = quality;
        this.quantity = quantity;
    }

    public long getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
