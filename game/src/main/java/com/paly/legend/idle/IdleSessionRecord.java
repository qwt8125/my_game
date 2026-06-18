package com.paly.legend.idle;

import java.time.LocalDateTime;

public class IdleSessionRecord {

    private long id;
    private long characterId;
    private String mapId;
    private String monsterId;
    private LocalDateTime startedAt;
    private LocalDateTime lastClaimedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastClaimedAt() {
        return lastClaimedAt;
    }

    public void setLastClaimedAt(LocalDateTime lastClaimedAt) {
        this.lastClaimedAt = lastClaimedAt;
    }
}
