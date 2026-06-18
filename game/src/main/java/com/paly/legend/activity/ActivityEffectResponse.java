package com.paly.legend.activity;

public class ActivityEffectResponse {

    private String type;
    private int percent;
    private String description;

    public ActivityEffectResponse() {
    }

    public ActivityEffectResponse(String type, int percent, String description) {
        this.type = type;
        this.percent = percent;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
