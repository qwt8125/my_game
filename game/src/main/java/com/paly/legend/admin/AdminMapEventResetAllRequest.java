package com.paly.legend.admin;

import javax.validation.constraints.Min;

public class AdminMapEventResetAllRequest {

    @Min(1)
    private long characterId;

    public long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
    }
}
