package com.paly.legend.guild;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.task.TaskRewardItemResponse;

public class GuildActivityClaimResponse {

    private boolean success;
    private String message;
    private String activityId;
    private int goldGained;
    private int currentGold;
    private List<TaskRewardItemResponse> items = new ArrayList<TaskRewardItemResponse>();

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

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
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
