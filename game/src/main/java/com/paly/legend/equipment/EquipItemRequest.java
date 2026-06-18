package com.paly.legend.equipment;

import javax.validation.constraints.Min;

public class EquipItemRequest {

    @Min(1)
    private long inventoryItemId;

    public long getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }
}

