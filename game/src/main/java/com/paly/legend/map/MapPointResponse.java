package com.paly.legend.map;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.config.EncounterMonsterConfig;

public class MapPointResponse {

    private String id;
    private String name;
    private String type;
    private int x;
    private int y;
    private String sprite;
    private boolean locked;
    private boolean completed;
    private boolean coolingDown;
    private String statusText;
    private String nextAvailableAt;
    private String description;
    private List<String> targetMonsterIds = new ArrayList<String>();
    private List<EncounterMonsterConfig> encounterMonsters = new ArrayList<EncounterMonsterConfig>();
    private String targetMapId;
    private List<String> taskIds = new ArrayList<String>();
    private List<String> nextEventIds = new ArrayList<String>();
    private int encounterMinCount;
    private int encounterMaxCount;
    private double encounterEliteChance;
    private int encounterIntervalSeconds;

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getSprite() {
        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCoolingDown() {
        return coolingDown;
    }

    public void setCoolingDown(boolean coolingDown) {
        this.coolingDown = coolingDown;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getNextAvailableAt() {
        return nextAvailableAt;
    }

    public void setNextAvailableAt(String nextAvailableAt) {
        this.nextAvailableAt = nextAvailableAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTargetMonsterIds() {
        return targetMonsterIds;
    }

    public void setTargetMonsterIds(List<String> targetMonsterIds) {
        this.targetMonsterIds = targetMonsterIds;
    }

    public List<EncounterMonsterConfig> getEncounterMonsters() {
        return encounterMonsters;
    }

    public void setEncounterMonsters(List<EncounterMonsterConfig> encounterMonsters) {
        this.encounterMonsters = encounterMonsters;
    }

    public String getTargetMapId() {
        return targetMapId;
    }

    public void setTargetMapId(String targetMapId) {
        this.targetMapId = targetMapId;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public List<String> getNextEventIds() {
        return nextEventIds;
    }

    public void setNextEventIds(List<String> nextEventIds) {
        this.nextEventIds = nextEventIds;
    }

    public int getEncounterMinCount() {
        return encounterMinCount;
    }

    public void setEncounterMinCount(int encounterMinCount) {
        this.encounterMinCount = encounterMinCount;
    }

    public int getEncounterMaxCount() {
        return encounterMaxCount;
    }

    public void setEncounterMaxCount(int encounterMaxCount) {
        this.encounterMaxCount = encounterMaxCount;
    }

    public double getEncounterEliteChance() {
        return encounterEliteChance;
    }

    public void setEncounterEliteChance(double encounterEliteChance) {
        this.encounterEliteChance = encounterEliteChance;
    }

    public int getEncounterIntervalSeconds() {
        return encounterIntervalSeconds;
    }

    public void setEncounterIntervalSeconds(int encounterIntervalSeconds) {
        this.encounterIntervalSeconds = encounterIntervalSeconds;
    }
}
