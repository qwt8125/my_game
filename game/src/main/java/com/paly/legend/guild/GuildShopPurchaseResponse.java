package com.paly.legend.guild;

public class GuildShopPurchaseResponse {

    private boolean success;
    private String message;
    private String shopItemId;
    private String itemId;
    private String itemName;
    private int quantity;
    private int goldCost;
    private int contributionCost;
    private int currentGold;
    private int currentContribution;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShopItemId() {
        return shopItemId;
    }

    public void setShopItemId(String shopItemId) {
        this.shopItemId = shopItemId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public int getContributionCost() {
        return contributionCost;
    }

    public void setContributionCost(int contributionCost) {
        this.contributionCost = contributionCost;
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public void setCurrentGold(int currentGold) {
        this.currentGold = currentGold;
    }

    public int getCurrentContribution() {
        return currentContribution;
    }

    public void setCurrentContribution(int currentContribution) {
        this.currentContribution = currentContribution;
    }
}
