package com.paly.legend.mail;

public class MailClaimResponse {

    private int goldGained;
    private String itemId;
    private String itemName;
    private int quantity;
    private int currentGold;

    public int getGoldGained() { return goldGained; }
    public void setGoldGained(int goldGained) { this.goldGained = goldGained; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getCurrentGold() { return currentGold; }
    public void setCurrentGold(int currentGold) { this.currentGold = currentGold; }
}
