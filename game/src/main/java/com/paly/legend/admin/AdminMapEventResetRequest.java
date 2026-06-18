package com.paly.legend.admin;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class AdminMapEventResetRequest {

    @Min(1)
    private long characterId;

    @NotBlank
    private String eventId;

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
}
