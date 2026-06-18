package com.paly.legend.inventory;

import javax.validation.constraints.Min;

public class SellMaterialsRequestItem {

    @Min(1)
    private long inventoryItemId;

    @Min(1)
    private int quantity;

    public long getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
