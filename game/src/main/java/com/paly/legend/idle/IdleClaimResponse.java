package com.paly.legend.idle;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.task.TaskRewardItemResponse;

public class IdleClaimResponse {

    private int expGained;
    private int goldGained;
    private int bonusExp;
    private int bonusGold;
    private int levelBefore;
    private int levelAfter;
    private int currentExp;
    private int currentGold;
    private int power;
    private long settledSeconds;
    private List<TaskRewardItemResponse> items = new ArrayList<TaskRewardItemResponse>();

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

    public int getBonusExp() {
        return bonusExp;
    }

    public void setBonusExp(int bonusExp) {
        this.bonusExp = bonusExp;
    }

    public int getBonusGold() {
        return bonusGold;
    }

    public void setBonusGold(int bonusGold) {
        this.bonusGold = bonusGold;
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

    public long getSettledSeconds() {
        return settledSeconds;
    }

    public void setSettledSeconds(long settledSeconds) {
        this.settledSeconds = settledSeconds;
    }

    public List<TaskRewardItemResponse> getItems() {
        return items;
    }

    public void setItems(List<TaskRewardItemResponse> items) {
        this.items = items;
    }
}
