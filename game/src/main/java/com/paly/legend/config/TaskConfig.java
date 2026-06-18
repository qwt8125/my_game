package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class TaskConfig {

    private String id;
    private String name;
    private String story;
    private String guide;
    private String type;
    private String targetId;
    private int targetCount;
    private int targetLevel;
    private List<String> preTaskIds = new ArrayList<String>();
    private TaskRewardConfig rewards;

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

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getGuide() {
        return guide;
    }

    public void setGuide(String guide) {
        this.guide = guide;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public List<String> getPreTaskIds() {
        return preTaskIds;
    }

    public void setPreTaskIds(List<String> preTaskIds) {
        this.preTaskIds = preTaskIds;
    }

    public TaskRewardConfig getRewards() {
        return rewards;
    }

    public void setRewards(TaskRewardConfig rewards) {
        this.rewards = rewards;
    }
}
