package com.paly.legend.worldboss;

import java.util.ArrayList;
import java.util.List;

public class WorldBossResponse {

    private String id;
    private String name;
    private String mapId;
    private String mapName;
    private String monsterId;
    private String monsterName;
    private int requiredLevel;
    private int requiredPower;
    private int rewardMultiplier;
    private int activityRewardBonusPercent;
    private int respawnMinutes;
    private String status;
    private boolean available;
    private int currentHp;
    private int maxHp;
    private String availableAt;
    private List<WorldBossDamageRankResponse> ranks = new ArrayList<WorldBossDamageRankResponse>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public String getMonsterId() { return monsterId; }
    public void setMonsterId(String monsterId) { this.monsterId = monsterId; }
    public String getMonsterName() { return monsterName; }
    public void setMonsterName(String monsterName) { this.monsterName = monsterName; }
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    public int getRequiredPower() { return requiredPower; }
    public void setRequiredPower(int requiredPower) { this.requiredPower = requiredPower; }
    public int getRewardMultiplier() { return rewardMultiplier; }
    public void setRewardMultiplier(int rewardMultiplier) { this.rewardMultiplier = rewardMultiplier; }
    public int getActivityRewardBonusPercent() { return activityRewardBonusPercent; }
    public void setActivityRewardBonusPercent(int activityRewardBonusPercent) { this.activityRewardBonusPercent = activityRewardBonusPercent; }
    public int getRespawnMinutes() { return respawnMinutes; }
    public void setRespawnMinutes(int respawnMinutes) { this.respawnMinutes = respawnMinutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public String getAvailableAt() { return availableAt; }
    public void setAvailableAt(String availableAt) { this.availableAt = availableAt; }
    public List<WorldBossDamageRankResponse> getRanks() { return ranks; }
    public void setRanks(List<WorldBossDamageRankResponse> ranks) { this.ranks = ranks; }
}
