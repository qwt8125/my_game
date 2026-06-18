package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class MapEventConfig {

    private String id;
    private String mapId;
    private String name;
    private String type;
    private int x;
    private int y;
    private String sprite;
    private String dialogue;
    private List<String> targetMonsterIds = new ArrayList<String>();
    private List<EncounterMonsterConfig> encounterMonsters = new ArrayList<EncounterMonsterConfig>();
    private String targetMapId;
    private List<String> requiredTaskIds = new ArrayList<String>();
    private List<String> nextEventIds = new ArrayList<String>();
    private boolean repeatable = true;
    private int cooldownSeconds;
    private int rewardGold;
    private int rewardExp;
    private List<TaskRewardItemConfig> rewardItems = new ArrayList<TaskRewardItemConfig>();
    private String resetType;
    private int encounterMinCount = 1;
    private int encounterMaxCount = 3;
    private double encounterEliteChance = 0.18;
    private int encounterIntervalSeconds = 5;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
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

    public String getDialogue() {
        return dialogue;
    }

    public void setDialogue(String dialogue) {
        this.dialogue = dialogue;
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

    public List<String> getRequiredTaskIds() {
        return requiredTaskIds;
    }

    public void setRequiredTaskIds(List<String> requiredTaskIds) {
        this.requiredTaskIds = requiredTaskIds;
    }

    public List<String> getNextEventIds() {
        return nextEventIds;
    }

    public void setNextEventIds(List<String> nextEventIds) {
        this.nextEventIds = nextEventIds;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public void setRewardExp(int rewardExp) {
        this.rewardExp = rewardExp;
    }

    public List<TaskRewardItemConfig> getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(List<TaskRewardItemConfig> rewardItems) {
        this.rewardItems = rewardItems;
    }

    public String getResetType() {
        return resetType;
    }

    public void setResetType(String resetType) {
        this.resetType = resetType;
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
