package com.paly.legend.admin;

import javax.validation.constraints.Min;

public class AdminGrantGoldRequest {

    @Min(1)
    private long characterId;

    @Min(1)
    private int gold;

    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
}
