package com.paly.legend.config;

import java.util.List;

public class ActivityRankingRewardConfig {

    private String rankingType;
    private int maxRank;
    private int rewardGold;
    private List<TaskRewardItemConfig> rewardItems;
    private String description;

    public String getRankingType() {
        return rankingType;
    }

    public void setRankingType(String rankingType) {
        this.rankingType = rankingType;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public void setMaxRank(int maxRank) {
        this.maxRank = maxRank;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }

    public List<TaskRewardItemConfig> getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(List<TaskRewardItemConfig> rewardItems) {
        this.rewardItems = rewardItems;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
