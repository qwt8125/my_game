package com.paly.legend.inventory;

import javax.validation.constraints.Min;

public class DiscardItemRequest {

    @Min(1)
    private int quantity = 1;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
