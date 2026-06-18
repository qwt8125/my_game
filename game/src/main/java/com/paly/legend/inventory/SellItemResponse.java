package com.paly.legend.inventory;

public class SellItemResponse {

    private int goldGained;
    private int currentGold;
    private int remainingQuantity;

    public SellItemResponse() {
    }

    public SellItemResponse(int goldGained, int currentGold, int remainingQuantity) {
        this.goldGained = goldGained;
        this.currentGold = currentGold;
        this.remainingQuantity = remainingQuantity;
    }

    public int getGoldGained() {
        return goldGained;
    }

    public void setGoldGained(int goldGained) {
        this.goldGained = goldGained;
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public void setCurrentGold(int currentGold) {
        this.currentGold = currentGold;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}

