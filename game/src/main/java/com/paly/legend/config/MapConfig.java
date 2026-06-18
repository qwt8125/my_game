package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class MapConfig {

    private String id;
    private String name;
    private int requiredLevel;
    private int recommendedPower;
    private int width = 1600;
    private int height = 1000;
    private String backgroundSprite;
    private List<String> monsterIds = new ArrayList<String>();

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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getBackgroundSprite() {
        return backgroundSprite;
    }

    public void setBackgroundSprite(String backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public List<String> getMonsterIds() {
        return monsterIds;
    }

    public void setMonsterIds(List<String> monsterIds) {
        this.monsterIds = monsterIds;
    }
}
