package com.paly.legend.map;

public class MapEventTriggerResponse {

    private String eventId;
    private String eventName;
    private String action;
    private String message;
    private String mapId;
    private String monsterId;
    private String monsterName;
    private String targetMapId;
    private int expGained;
    private int goldGained;
    private int levelBefore;
    private int levelAfter;
    private int currentExp;
    private int currentGold;
    private int power;
    private java.util.List<MapEventRewardItemResponse> items = new java.util.ArrayList<MapEventRewardItemResponse>();

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
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

    public String getTargetMapId() {
        return targetMapId;
    }

    public void setTargetMapId(String targetMapId) {
        this.targetMapId = targetMapId;
    }

    public int getGoldGained() {
        return goldGained;
    }

    public void setGoldGained(int goldGained) {
        this.goldGained = goldGained;
    }

    public int getExpGained() {
        return expGained;
    }

    public void setExpGained(int expGained) {
        this.expGained = expGained;
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

    public java.util.List<MapEventRewardItemResponse> getItems() {
        return items;
    }

    public void setItems(java.util.List<MapEventRewardItemResponse> items) {
        this.items = items;
    }
}
