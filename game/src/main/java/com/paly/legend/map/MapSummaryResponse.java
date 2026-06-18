package com.paly.legend.map;

import com.paly.legend.config.MapConfig;

public class MapSummaryResponse {

    private String id;
    private String name;
    private int requiredLevel;
    private int recommendedPower;

    public static MapSummaryResponse from(MapConfig map) {
        MapSummaryResponse response = new MapSummaryResponse();
        response.setId(map.getId());
        response.setName(map.getName());
        response.setRequiredLevel(map.getRequiredLevel());
        response.setRecommendedPower(map.getRecommendedPower());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getRecommendedPower() {
        return recommendedPower;
    }

    public void setRecommendedPower(int recommendedPower) {
        this.recommendedPower = recommendedPower;
    }
}

