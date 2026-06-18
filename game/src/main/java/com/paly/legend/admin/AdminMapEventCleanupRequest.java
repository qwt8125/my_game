package com.paly.legend.admin;

import javax.validation.constraints.Min;

public class AdminMapEventCleanupRequest {

    @Min(1)
    private long characterId;

    private int keepDays = 7;

    public long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
    }

    public int getKeepDays() {
        return keepDays;
    }

    public void setKeepDays(int keepDays) {
        this.keepDays = keepDays;
    }
}
