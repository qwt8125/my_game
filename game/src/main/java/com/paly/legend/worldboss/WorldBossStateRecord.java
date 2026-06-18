package com.paly.legend.worldboss;

import java.time.LocalDateTime;

public class WorldBossStateRecord {

    private String bossId;
    private String status;
    private int currentHp;
    private int maxHp;
    private LocalDateTime availableAt;
    private LocalDateTime killedAt;
    private boolean rewardsSent;

    public String getBossId() { return bossId; }
    public void setBossId(String bossId) { this.bossId = bossId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public LocalDateTime getAvailableAt() { return availableAt; }
    public void setAvailableAt(LocalDateTime availableAt) { this.availableAt = availableAt; }
    public LocalDateTime getKilledAt() { return killedAt; }
    public void setKilledAt(LocalDateTime killedAt) { this.killedAt = killedAt; }
    public boolean isRewardsSent() { return rewardsSent; }
    public void setRewardsSent(boolean rewardsSent) { this.rewardsSent = rewardsSent; }
}
