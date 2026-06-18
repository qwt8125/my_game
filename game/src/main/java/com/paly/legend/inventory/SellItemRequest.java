package com.paly.legend.inventory;

import javax.validation.constraints.Min;

public class SellItemRequest {

    @Min(1)
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

