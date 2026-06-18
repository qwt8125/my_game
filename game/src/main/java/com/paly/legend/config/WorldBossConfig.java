package com.paly.legend.config;

public class WorldBossConfig {

    private String id;
    private String name;
    private String mapId;
    private String monsterId;
    private int respawnMinutes;
    private int rewardMultiplier;
    private int requiredLevel;
    private int requiredPower;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public String getMonsterId() { return monsterId; }
    public void setMonsterId(String monsterId) { this.monsterId = monsterId; }
    public int getRespawnMinutes() { return respawnMinutes; }
    public void setRespawnMinutes(int respawnMinutes) { this.respawnMinutes = respawnMinutes; }
    public int getRewardMultiplier() { return rewardMultiplier; }
    public void setRewardMultiplier(int rewardMultiplier) { this.rewardMultiplier = rewardMultiplier; }
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    public int getRequiredPower() { return requiredPower; }
    public void setRequiredPower(int requiredPower) { this.requiredPower = requiredPower; }
}
