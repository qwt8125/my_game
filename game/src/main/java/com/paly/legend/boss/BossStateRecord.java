package com.paly.legend.boss;

import java.time.LocalDateTime;

public class BossStateRecord {

    private String bossId;
    private LocalDateTime availableAt;
    private Long lastKilledBy;

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }

    public LocalDateTime getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(LocalDateTime availableAt) {
        this.availableAt = availableAt;
    }

    public Long getLastKilledBy() {
        return lastKilledBy;
    }

    public void setLastKilledBy(Long lastKilledBy) {
        this.lastKilledBy = lastKilledBy;
    }
}
