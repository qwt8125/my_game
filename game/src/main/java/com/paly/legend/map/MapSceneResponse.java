package com.paly.legend.map;

import java.util.ArrayList;
import java.util.List;

public class MapSceneResponse {

    private String id;
    private String name;
    private int requiredLevel;
    private int recommendedPower;
    private int width;
    private int height;
    private String backgroundSprite;
    private boolean locked;
    private PlayerMapPositionResponse player;
    private List<MapPointResponse> npcs = new ArrayList<MapPointResponse>();
    private List<MapPointResponse> events = new ArrayList<MapPointResponse>();
    private List<MonsterResponse> monsters = new ArrayList<MonsterResponse>();

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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public PlayerMapPositionResponse getPlayer() {
        return player;
    }

    public void setPlayer(PlayerMapPositionResponse player) {
        this.player = player;
    }

    public List<MapPointResponse> getNpcs() {
        return npcs;
    }

    public void setNpcs(List<MapPointResponse> npcs) {
        this.npcs = npcs;
    }

    public List<MapPointResponse> getEvents() {
        return events;
    }

    public void setEvents(List<MapPointResponse> events) {
        this.events = events;
    }

    public List<MonsterResponse> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<MonsterResponse> monsters) {
        this.monsters = monsters;
    }
}
