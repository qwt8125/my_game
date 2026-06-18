package com.paly.legend.inventory;

public class DiscardItemResponse {

    private String itemId;
    private String name;
    private int discardedQuantity;
    private int remainingQuantity;

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

    public int getDiscardedQuantity() {
        return discardedQuantity;
    }

    public void setDiscardedQuantity(int discardedQuantity) {
        this.discardedQuantity = discardedQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}
