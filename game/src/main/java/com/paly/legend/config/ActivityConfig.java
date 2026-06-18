package com.paly.legend.config;

import java.util.List;

public class ActivityConfig {

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
    private List<TaskRewardItemConfig> rewardItems;
    private List<ActivityEffectConfig> effects;
    private List<ActivityRankingRewardConfig> rankingRewards;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTargetView() {
        return targetView;
    }

    public void setTargetView(String targetView) {
        this.targetView = targetView;
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

    public List<ActivityEffectConfig> getEffects() {
        return effects;
    }

    public void setEffects(List<ActivityEffectConfig> effects) {
        this.effects = effects;
    }

    public List<ActivityRankingRewardConfig> getRankingRewards() {
        return rankingRewards;
    }

    public void setRankingRewards(List<ActivityRankingRewardConfig> rankingRewards) {
        this.rankingRewards = rankingRewards;
    }
}
