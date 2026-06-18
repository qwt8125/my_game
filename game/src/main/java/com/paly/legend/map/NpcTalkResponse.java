package com.paly.legend.map;

import java.util.ArrayList;
import java.util.List;

public class NpcTalkResponse {

    private String npcId;
    private String npcName;
    private String mapId;
    private List<String> dialogueLines = new ArrayList<String>();
    private List<String> taskIds = new ArrayList<String>();

    public String getNpcId() {
        return npcId;
    }

    public void setNpcId(String npcId) {
        this.npcId = npcId;
    }

    public String getNpcName() {
        return npcName;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
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
}
