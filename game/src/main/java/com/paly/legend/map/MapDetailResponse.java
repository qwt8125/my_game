package com.paly.legend.map;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MonsterConfig;

public class MapDetailResponse {

    private String id;
    private String name;
    private int requiredLevel;
    private int recommendedPower;
    private List<MonsterResponse> monsters = new ArrayList<MonsterResponse>();

    public static MapDetailResponse from(MapConfig map, List<MonsterConfig> monsters) {
        MapDetailResponse response = new MapDetailResponse();
        response.setId(map.getId());
        response.setName(map.getName());
        response.setRequiredLevel(map.getRequiredLevel());
        response.setRecommendedPower(map.getRecommendedPower());
        for (MonsterConfig monster : monsters) {
            response.getMonsters().add(MonsterResponse.from(monster));
        }
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

    public List<MonsterResponse> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<MonsterResponse> monsters) {
        this.monsters = monsters;
    }
}

