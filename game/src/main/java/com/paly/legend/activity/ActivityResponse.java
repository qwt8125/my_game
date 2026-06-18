package com.paly.legend.activity;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.task.TaskRewardItemResponse;

public class ActivityResponse {

    private String id;
    private String name;
    private String type;
    private String status;
    private String tag;
    private String summary;
    private String description;
    private String startAt;
    private String endAt;
    private int priority;
    private String targetView;
    private int rewardGold;
    private List<TaskRewardItemResponse> rewardItems = new ArrayList<TaskRewardItemResponse>();
    private List<ActivityEffectResponse> effects = new ArrayList<ActivityEffectResponse>();
    private List<ActivityRankingRewardResponse> rankingRewards = new ArrayList<ActivityRankingRewardResponse>();
    private boolean claimed;
    private boolean claimable;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartAt() { return startAt; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public String getEndAt() { return endAt; }
    public void setEndAt(String endAt) { this.endAt = endAt; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getTargetView() { return targetView; }
    public void setTargetView(String targetView) { this.targetView = targetView; }
    public int getRewardGold() { return rewardGold; }
    public void setRewardGold(int rewardGold) { this.rewardGold = rewardGold; }
    public List<TaskRewardItemResponse> getRewardItems() { return rewardItems; }
    public void setRewardItems(List<TaskRewardItemResponse> rewardItems) {
        this.rewardItems = rewardItems == null ? new ArrayList<TaskRewardItemResponse>() : rewardItems;
    }
    public List<ActivityEffectResponse> getEffects() { return effects; }
    public void setEffects(List<ActivityEffectResponse> effects) {
        this.effects = effects == null ? new ArrayList<ActivityEffectResponse>() : effects;
    }
    public List<ActivityRankingRewardResponse> getRankingRewards() { return rankingRewards; }
    public void setRankingRewards(List<ActivityRankingRewardResponse> rankingRewards) {
        this.rankingRewards = rankingRewards == null ? new ArrayList<ActivityRankingRewardResponse>() : rankingRewards;
    }
    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
    public boolean isClaimable() { return claimable; }
    public void setClaimable(boolean claimable) { this.claimable = claimable; }
}
