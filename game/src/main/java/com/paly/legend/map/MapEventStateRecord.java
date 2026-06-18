package com.paly.legend.map;

import java.time.LocalDateTime;

public class MapEventStateRecord {

    private long id;
    private long characterId;
    private String eventId;
    private int triggerCount;
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime nextAvailableAt;
    private boolean completed;

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

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(int triggerCount) {
        this.triggerCount = triggerCount;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public LocalDateTime getNextAvailableAt() {
        return nextAvailableAt;
    }

    public void setNextAvailableAt(LocalDateTime nextAvailableAt) {
        this.nextAvailableAt = nextAvailableAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
