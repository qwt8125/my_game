package com.paly.legend.config;

public class GuildShopItemConfig {

    private String id;
    private String itemId;
    private int quantity;
    private int contributionCost;
    private int goldCost;
    private int dailyLimit;
    private int minContribution;
    private int sortOrder;
    private String tag;
    private String description;

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

    public int getMinContribution() {
        return minContribution;
    }

    public void setMinContribution(int minContribution) {
        this.minContribution = minContribution;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
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
}
