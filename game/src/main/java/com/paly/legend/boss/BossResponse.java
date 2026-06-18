package com.paly.legend.boss;

public class BossResponse {

    private String id;
    private String name;
    private String mapId;
    private String mapName;
    private String monsterId;
    private String monsterName;
    private boolean available;
    private String availableAt;
    private int respawnMinutes;
    private int rewardMultiplier;

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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(String availableAt) {
        this.availableAt = availableAt;
    }

    public int getRespawnMinutes() {
        return respawnMinutes;
    }

    public void setRespawnMinutes(int respawnMinutes) {
        this.respawnMinutes = respawnMinutes;
    }

    public int getRewardMultiplier() {
        return rewardMultiplier;
    }

    public void setRewardMultiplier(int rewardMultiplier) {
        this.rewardMultiplier = rewardMultiplier;
    }
}
