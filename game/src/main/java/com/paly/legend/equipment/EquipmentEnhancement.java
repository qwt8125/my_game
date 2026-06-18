package com.paly.legend.equipment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.inventory.InventoryItemRecord;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EquipmentEnhancement {

    private static final ObjectMapper FALLBACK_OBJECT_MAPPER = new ObjectMapper();

    private int enhanceLevel;
    private List<EquipmentAffix> affixes = new ArrayList<EquipmentAffix>();

    public int getEnhanceLevel() {
        return enhanceLevel;
    }

    public void setEnhanceLevel(int enhanceLevel) {
        this.enhanceLevel = enhanceLevel;
    }

    public List<EquipmentAffix> getAffixes() {
        return affixes;
    }

    public void setAffixes(List<EquipmentAffix> affixes) {
        this.affixes = affixes == null ? new ArrayList<EquipmentAffix>() : affixes;
    }

    public static EquipmentEnhancement read(ObjectMapper objectMapper, InventoryItemRecord record) {
        if (record == null || record.getExtraJson() == null || record.getExtraJson().trim().isEmpty()) {
            return new EquipmentEnhancement();
        }
        try {
            return normalize(objectMapper.readValue(record.getExtraJson(), EquipmentEnhancement.class));
        } catch (IOException ex) {
            return new EquipmentEnhancement();
        }
    }

    public static EquipmentEnhancement readFromJson(String extraJson) {
        if (extraJson == null || extraJson.trim().isEmpty()) {
            return new EquipmentEnhancement();
        }
        try {
            return normalize(FALLBACK_OBJECT_MAPPER.readValue(extraJson, EquipmentEnhancement.class));
        } catch (IOException ex) {
            return new EquipmentEnhancement();
        }
    }

    private static EquipmentEnhancement normalize(EquipmentEnhancement enhancement) {
        if (enhancement == null) {
            return new EquipmentEnhancement();
        }
        if (enhancement.getAffixes() == null) {
            enhancement.setAffixes(new ArrayList<EquipmentAffix>());
        }
        return enhancement;
    }

    public static int levelFromJson(String extraJson) {
        if (extraJson == null) {
            return 0;
        }
        String marker = "\"enhanceLevel\":";
        int index = extraJson.indexOf(marker);
        if (index < 0) {
            return 0;
        }
        int start = index + marker.length();
        int end = start;
        while (end < extraJson.length() && Character.isDigit(extraJson.charAt(end))) {
            end++;
        }
        if (end == start) {
            return 0;
        }
        try {
            return Integer.parseInt(extraJson.substring(start, end));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static int attack(ItemConfig item, int enhanceLevel) {
        return item.getAttack() + enhanceLevel * 2;
    }

    public static int attack(ItemConfig item, EquipmentEnhancement enhancement) {
        return attack(item, safeLevel(enhancement)) + intAffix(enhancement, "attack");
    }

    public static int defense(ItemConfig item, int enhanceLevel) {
        return item.getDefense() + enhanceLevel;
    }

    public static int defense(ItemConfig item, EquipmentEnhancement enhancement) {
        return defense(item, safeLevel(enhancement)) + intAffix(enhancement, "defense");
    }

    public static int attackSpeed(ItemConfig item, int enhanceLevel) {
        return item.getAttackSpeed() + enhanceLevel;
    }

    public static int attackSpeed(ItemConfig item, EquipmentEnhancement enhancement) {
        return attackSpeed(item, safeLevel(enhancement)) + intAffix(enhancement, "attackSpeed");
    }

    public static int hp(ItemConfig item, int enhanceLevel) {
        return item.getHp() + enhanceLevel * 10;
    }

    public static int hp(ItemConfig item, EquipmentEnhancement enhancement) {
        return hp(item, safeLevel(enhancement)) + intAffix(enhancement, "hp");
    }

    public static double skillTriggerBonus(EquipmentEnhancement enhancement) {
        return affixValue(enhancement, "skillTriggerBonus");
    }

    private static int safeLevel(EquipmentEnhancement enhancement) {
        return enhancement == null ? 0 : Math.max(0, enhancement.getEnhanceLevel());
    }

    private static int intAffix(EquipmentEnhancement enhancement, String stat) {
        return (int) Math.round(affixValue(enhancement, stat));
    }

    private static double affixValue(EquipmentEnhancement enhancement, String stat) {
        if (enhancement == null || enhancement.getAffixes() == null) {
            return 0;
        }
        double total = 0;
        for (EquipmentAffix affix : enhancement.getAffixes()) {
            if (affix != null && stat.equals(affix.getStat())) {
                total += affix.getValue();
            }
        }
        return total;
    }

    public static int costForNextLevel(int currentLevel) {
        if (currentLevel <= 0) {
            return 20;
        }
        if (currentLevel == 1) {
            return 50;
        }
        return 100;
    }
}
