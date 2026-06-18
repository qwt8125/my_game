package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class NpcConfig {

    private String id;
    private String name;
    private String mapId;
    private String description;
    private int x;
    private int y;
    private String sprite;
    private List<String> dialogueLines = new ArrayList<String>();
    private List<String> taskIds = new ArrayList<String>();
    private List<String> requiredTaskIds = new ArrayList<String>();

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

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getSprite() {
        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public List<String> getDialogueLines() {
        return dialogueLines;
    }

    public void setDialogueLines(List<String> dialogueLines) {
        this.dialogueLines = dialogueLines;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public List<String> getRequiredTaskIds() {
        return requiredTaskIds;
    }

    public void setRequiredTaskIds(List<String> requiredTaskIds) {
        this.requiredTaskIds = requiredTaskIds;
    }
}
