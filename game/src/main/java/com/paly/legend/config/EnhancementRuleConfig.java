package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class EnhancementRuleConfig {

    private int nextLevel;
    private String quality;
    private int minRequiredLevel;
    private int goldCost;
    private List<TaskRewardItemConfig> materialCosts = new ArrayList<TaskRewardItemConfig>();

    public int getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(int nextLevel) {
        this.nextLevel = nextLevel;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public int getMinRequiredLevel() {
        return minRequiredLevel;
    }

    public void setMinRequiredLevel(int minRequiredLevel) {
        this.minRequiredLevel = minRequiredLevel;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public List<TaskRewardItemConfig> getMaterialCosts() {
        return materialCosts;
    }

    public void setMaterialCosts(List<TaskRewardItemConfig> materialCosts) {
        this.materialCosts = materialCosts;
    }
}
