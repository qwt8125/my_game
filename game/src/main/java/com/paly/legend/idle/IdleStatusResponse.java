package com.paly.legend.idle;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.task.TaskRewardItemResponse;

public class IdleStatusResponse {

    private boolean active;
    private String mapId;
    private String mapName;
    private String monsterId;
    private String monsterName;
    private long elapsedSeconds;
    private long cappedSeconds;
    private int estimatedExp;
    private int estimatedGold;
    private int estimatedBonusExp;
    private int estimatedBonusGold;
    private List<TaskRewardItemResponse> estimatedItems = new ArrayList<TaskRewardItemResponse>();

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(String monsterId) {
        this.monsterId = monsterId;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public long getCappedSeconds() {
        return cappedSeconds;
    }

    public void setCappedSeconds(long cappedSeconds) {
        this.cappedSeconds = cappedSeconds;
    }

    public int getEstimatedExp() {
        return estimatedExp;
    }

    public void setEstimatedExp(int estimatedExp) {
        this.estimatedExp = estimatedExp;
    }

    public int getEstimatedGold() {
        return estimatedGold;
    }

    public void setEstimatedGold(int estimatedGold) {
        this.estimatedGold = estimatedGold;
    }

    public int getEstimatedBonusExp() {
        return estimatedBonusExp;
    }

    public void setEstimatedBonusExp(int estimatedBonusExp) {
        this.estimatedBonusExp = estimatedBonusExp;
    }

    public int getEstimatedBonusGold() {
        return estimatedBonusGold;
    }

    public void setEstimatedBonusGold(int estimatedBonusGold) {
        this.estimatedBonusGold = estimatedBonusGold;
    }

    public List<TaskRewardItemResponse> getEstimatedItems() {
        return estimatedItems;
    }

    public void setEstimatedItems(List<TaskRewardItemResponse> estimatedItems) {
        this.estimatedItems = estimatedItems;
    }
}
