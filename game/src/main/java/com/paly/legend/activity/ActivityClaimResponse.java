package com.paly.legend.activity;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.task.TaskRewardItemResponse;

public class ActivityClaimResponse {

    private String activityId;
    private String rankingType;
    private int currentRank;
    private int goldGained;
    private int currentGold;
    private List<TaskRewardItemResponse> items = new ArrayList<TaskRewardItemResponse>();

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getRankingType() {
        return rankingType;
    }

    public void setRankingType(String rankingType) {
        this.rankingType = rankingType;
    }

    public int getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
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

    public List<TaskRewardItemResponse> getItems() {
        return items;
    }

    public void setItems(List<TaskRewardItemResponse> items) {
        this.items = items == null ? new ArrayList<TaskRewardItemResponse>() : items;
    }
}
