package com.paly.legend.activity;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.task.TaskRewardItemResponse;

public class ActivityRankingRewardResponse {

    private String rankingType;
    private int maxRank;
    private int currentRank;
    private boolean eligible;
    private int rewardGold;
    private List<TaskRewardItemResponse> rewardItems = new ArrayList<TaskRewardItemResponse>();
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

    public int getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }

    public List<TaskRewardItemResponse> getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(List<TaskRewardItemResponse> rewardItems) {
        this.rewardItems = rewardItems == null ? new ArrayList<TaskRewardItemResponse>() : rewardItems;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
