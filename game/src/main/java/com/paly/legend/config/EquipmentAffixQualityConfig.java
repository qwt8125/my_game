package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class EquipmentAffixQualityConfig {

    private String quality;
    private int affixCount;
    private String rerollMaterialId;
    private int rerollMaterialQuantity;
    private List<EquipmentAffixStatConfig> stats = new ArrayList<EquipmentAffixStatConfig>();

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public int getAffixCount() {
        return affixCount;
    }

    public void setAffixCount(int affixCount) {
        this.affixCount = affixCount;
    }

    public String getRerollMaterialId() {
        return rerollMaterialId;
    }

    public void setRerollMaterialId(String rerollMaterialId) {
        this.rerollMaterialId = rerollMaterialId;
    }

    public int getRerollMaterialQuantity() {
        return rerollMaterialQuantity;
    }

    public void setRerollMaterialQuantity(int rerollMaterialQuantity) {
        this.rerollMaterialQuantity = rerollMaterialQuantity;
    }

    public List<EquipmentAffixStatConfig> getStats() {
        return stats;
    }

    public void setStats(List<EquipmentAffixStatConfig> stats) {
        this.stats = stats;
    }
}
