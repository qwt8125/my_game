package com.paly.legend.task;

import java.util.ArrayList;
import java.util.List;

public class TaskResponse {

    private String id;
    private String name;
    private String story;
    private String guide;
    private String type;
    private String targetId;
    private String targetName;
    private String targetMapId;
    private String targetPointId;
    private String targetPointType;
    private int targetCount;
    private int targetLevel;
    private int currentCount;
    private int status;
    private String statusText;
    private boolean locked;
    private List<String> preTaskIds = new ArrayList<String>();
    private TaskRewardResponse rewards;

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

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetMapId() {
        return targetMapId;
    }

    public void setTargetMapId(String targetMapId) {
        this.targetMapId = targetMapId;
    }

    public String getTargetPointId() {
        return targetPointId;
    }

    public void setTargetPointId(String targetPointId) {
        this.targetPointId = targetPointId;
    }

    public String getTargetPointType() {
        return targetPointType;
    }

    public void setTargetPointType(String targetPointType) {
        this.targetPointType = targetPointType;
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

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<String> getPreTaskIds() {
        return preTaskIds;
    }

    public void setPreTaskIds(List<String> preTaskIds) {
        this.preTaskIds = preTaskIds;
    }

    public TaskRewardResponse getRewards() {
        return rewards;
    }

    public void setRewards(TaskRewardResponse rewards) {
        this.rewards = rewards;
    }
}
