package com.paly.legend.equipment;

import javax.validation.constraints.Positive;

public class RerollAffixRequest {

    @Positive
    private long inventoryItemId;

    public long getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }
}
