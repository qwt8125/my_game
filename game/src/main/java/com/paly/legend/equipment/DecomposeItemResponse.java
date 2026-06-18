package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.List;

public class DecomposeItemResponse {

    private long inventoryItemId;
    private String itemId;
    private String name;
    private int enhanceLevel;
    private List<EnhancementMaterialCost> materials = new ArrayList<EnhancementMaterialCost>();

    public long getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEnhanceLevel() {
        return enhanceLevel;
    }

    public void setEnhanceLevel(int enhanceLevel) {
        this.enhanceLevel = enhanceLevel;
    }

    public List<EnhancementMaterialCost> getMaterials() {
        return materials;
    }

    public void setMaterials(List<EnhancementMaterialCost> materials) {
        this.materials = materials;
    }
}
