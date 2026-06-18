package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class DropConfig {

    private String sourceType;
    private String sourceId;
    private List<DropItemConfig> items = new ArrayList<DropItemConfig>();

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public List<DropItemConfig> getItems() {
        return items;
    }

    public void setItems(List<DropItemConfig> items) {
        this.items = items;
    }
}

