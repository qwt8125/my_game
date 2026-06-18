package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.config.EquipmentAffixQualityConfig;
import com.paly.legend.config.EquipmentAffixStatConfig;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import org.springframework.stereotype.Service;

@Service
public class EquipmentAffixService {

    private final GameConfigService gameConfigService;
    private final ObjectMapper objectMapper;

    public EquipmentAffixService(GameConfigService gameConfigService, ObjectMapper objectMapper) {
        this.gameConfigService = gameConfigService;
        this.objectMapper = objectMapper;
    }

    public String initialExtraJson(ItemConfig item) {
        if (!"equipment".equals(item.getType())) {
            return "{}";
        }
        return toJson(reroll(item, new EquipmentEnhancement()));
    }

    public EquipmentEnhancement reroll(ItemConfig item, EquipmentEnhancement current) {
        EquipmentEnhancement next = new EquipmentEnhancement();
        next.setEnhanceLevel(current == null ? 0 : Math.max(0, current.getEnhanceLevel()));
        EquipmentAffixQualityConfig rule = gameConfigService.getEquipmentAffixRule(item.getQuality());
        if (rule == null) {
            return next;
        }
        next.setAffixes(rollAffixes(rule));
        return next;
    }

    public EnhancementMaterialCost rerollCost(ItemConfig item) {
        EquipmentAffixQualityConfig rule = gameConfigService.getEquipmentAffixRule(item.getQuality());
        if (rule == null) {
            return null;
        }
        ItemConfig material = gameConfigService.getItemRequired(rule.getRerollMaterialId());
        return new EnhancementMaterialCost(material.getId(), material.getName(), rule.getRerollMaterialQuantity());
    }

    public String toJson(EquipmentEnhancement enhancement) {
        try {
            return objectMapper.writeValueAsString(enhancement);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize equipment enhancement", ex);
        }
    }

    private List<EquipmentAffix> rollAffixes(EquipmentAffixQualityConfig rule) {
        List<EquipmentAffix> result = new ArrayList<EquipmentAffix>();
        List<EquipmentAffixStatConfig> pool = new ArrayList<EquipmentAffixStatConfig>(rule.getStats());
        int count = Math.min(Math.max(0, rule.getAffixCount()), pool.size());
        for (int i = 0; i < count; i++) {
            EquipmentAffixStatConfig selected = pickStat(pool);
            if (selected == null) {
                break;
            }
            pool.remove(selected);
            result.add(new EquipmentAffix(selected.getStat(), rollValue(selected)));
        }
        return result;
    }

    private EquipmentAffixStatConfig pickStat(List<EquipmentAffixStatConfig> pool) {
        int totalWeight = 0;
        for (EquipmentAffixStatConfig stat : pool) {
            totalWeight += Math.max(0, stat.getWeight());
        }
        if (totalWeight <= 0) {
            return null;
        }
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (EquipmentAffixStatConfig stat : pool) {
            cursor += Math.max(0, stat.getWeight());
            if (roll < cursor) {
                return stat;
            }
        }
        return pool.isEmpty() ? null : pool.get(0);
    }

    private double rollValue(EquipmentAffixStatConfig stat) {
        if ("skillTriggerBonus".equals(stat.getStat())) {
            int min = (int) Math.ceil(stat.getMin() * 100);
            int max = (int) Math.floor(stat.getMax() * 100);
            if (max <= min) {
                return min / 100.0;
            }
            return ThreadLocalRandom.current().nextInt(min, max + 1) / 100.0;
        }
        int min = (int) Math.ceil(stat.getMin());
        int max = (int) Math.floor(stat.getMax());
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
