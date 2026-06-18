package com.paly.legend.battle;

import javax.validation.constraints.NotBlank;

public class BattleRequest {

    @NotBlank
    private String mapId;

    @NotBlank
    private String monsterId;

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
}

