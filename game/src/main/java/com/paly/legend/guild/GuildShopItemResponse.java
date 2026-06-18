package com.paly.legend.guild;

public class GuildShopItemResponse {

    private String id;
    private String itemId;
    private String itemName;
    private String itemType;
    private int quantity;
    private int contributionCost;
    private int goldCost;
    private int dailyLimit;
    private int dailyUsed;
    private int remainingTimes;
    private int minContribution;
    private String tag;
    private String description;
    private boolean canBuy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getContributionCost() {
        return contributionCost;
    }

    public void setContributionCost(int contributionCost) {
        this.contributionCost = contributionCost;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public int getDailyUsed() {
        return dailyUsed;
    }

    public void setDailyUsed(int dailyUsed) {
        this.dailyUsed = dailyUsed;
    }

    public int getRemainingTimes() {
        return remainingTimes;
    }

    public void setRemainingTimes(int remainingTimes) {
        this.remainingTimes = remainingTimes;
    }

    public int getMinContribution() {
        return minContribution;
    }

    public void setMinContribution(int minContribution) {
        this.minContribution = minContribution;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCanBuy() {
        return canBuy;
    }

    public void setCanBuy(boolean canBuy) {
        this.canBuy = canBuy;
    }
}
