package com.paly.legend.task;

import java.util.ArrayList;
import java.util.List;

public class TaskClaimResponse {

    private String taskId;
    private int expGained;
    private int goldGained;
    private int levelBefore;
    private int levelAfter;
    private int currentExp;
    private int currentGold;
    private int power;
    private List<TaskRewardItemResponse> items = new ArrayList<TaskRewardItemResponse>();

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getExpGained() {
        return expGained;
    }

    public void setExpGained(int expGained) {
        this.expGained = expGained;
    }

    public int getGoldGained() {
        return goldGained;
    }

    public void setGoldGained(int goldGained) {
        this.goldGained = goldGained;
    }

    public int getLevelBefore() {
        return levelBefore;
    }

    public void setLevelBefore(int levelBefore) {
        this.levelBefore = levelBefore;
    }

    public int getLevelAfter() {
        return levelAfter;
    }

    public void setLevelAfter(int levelAfter) {
        this.levelAfter = levelAfter;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(int currentExp) {
        this.currentExp = currentExp;
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public void setCurrentGold(int currentGold) {
        this.currentGold = currentGold;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public List<TaskRewardItemResponse> getItems() {
        return items;
    }

    public void setItems(List<TaskRewardItemResponse> items) {
        this.items = items;
    }
}
