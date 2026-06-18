package com.paly.legend.inventory;

public class InventoryItemGrant {

    private final String itemId;
    private final int quantity;

    public InventoryItemGrant(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }
}
