package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class GuildActivityConfig {

    private String id;
    private String name;
    private String description;
    private int targetContribution;
    private int rewardGold;
    private List<TaskRewardItemConfig> rewardItems = new ArrayList<TaskRewardItemConfig>();
    private int sortOrder;
    private String tag;

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

    public int getTargetContribution() {
        return targetContribution;
    }

    public void setTargetContribution(int targetContribution) {
        this.targetContribution = targetContribution;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
