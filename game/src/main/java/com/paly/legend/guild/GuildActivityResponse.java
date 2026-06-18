package com.paly.legend.guild;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.task.TaskRewardItemResponse;

public class GuildActivityResponse {

    private String id;
    private String name;
    private String description;
    private String tag;
    private int targetContribution;
    private int currentContribution;
    private int progressPercent;
    private int rewardGold;
    private boolean achieved;
    private boolean claimed;
    private boolean claimable;
    private List<TaskRewardItemResponse> rewardItems = new ArrayList<TaskRewardItemResponse>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getTargetContribution() {
        return targetContribution;
    }

    public void setTargetContribution(int targetContribution) {
        this.targetContribution = targetContribution;
    }

    public int getCurrentContribution() {
        return currentContribution;
    }

    public void setCurrentContribution(int currentContribution) {
        this.currentContribution = currentContribution;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public boolean isClaimable() {
        return claimable;
    }

    public void setClaimable(boolean claimable) {
        this.claimable = claimable;
    }

    public List<TaskRewardItemResponse> getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(List<TaskRewardItemResponse> rewardItems) {
        this.rewardItems = rewardItems == null ? new ArrayList<TaskRewardItemResponse>() : rewardItems;
    }
}
