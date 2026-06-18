package com.paly.legend.battle;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

public class BattleEncounterRequest {

    @NotBlank
    private String mapId;
    private String eventId;
    private List<String> monsterIds = new ArrayList<String>();
    private int minCount = 1;
    private int maxCount = 3;
    private double eliteChance = 0.18;

    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public List<String> getMonsterIds() { return monsterIds; }
    public void setMonsterIds(List<String> monsterIds) { this.monsterIds = monsterIds; }
    public int getMinCount() { return minCount; }
    public void setMinCount(int minCount) { this.minCount = minCount; }
    public int getMaxCount() { return maxCount; }
    public void setMaxCount(int maxCount) { this.maxCount = maxCount; }
    public double getEliteChance() { return eliteChance; }
    public void setEliteChance(double eliteChance) { this.eliteChance = eliteChance; }
}
