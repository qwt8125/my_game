package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.paly.legend.config.EquipmentSetBonusConfig;
import com.paly.legend.config.ItemConfig;

public final class EquipmentSetBonusCalculator {

    private EquipmentSetBonusCalculator() {
    }

    public static List<EquipmentSetBonusResponse> bonusesFor(List<ItemConfig> equippedItems) {
        Map<String, SetState> states = collect(equippedItems);
        List<EquipmentSetBonusResponse> result = new ArrayList<EquipmentSetBonusResponse>();
        for (SetState state : states.values()) {
            for (EquipmentSetBonusConfig bonus : state.bonuses) {
                EquipmentSetBonusResponse response = new EquipmentSetBonusResponse();
                response.setSetId(state.setId);
                response.setSetName(state.setName);
                response.setPieces(state.pieces);
                response.setRequiredPieces(bonus.getPieces());
                response.setActive(state.pieces >= bonus.getPieces());
                response.setHp(bonus.getHp());
                response.setAttack(bonus.getAttack());
                response.setDefense(bonus.getDefense());
                response.setAttackSpeed(bonus.getAttackSpeed());
                result.add(response);
            }
        }
        return result;
    }

    public static CharacterStats activeStats(List<ItemConfig> equippedItems) {
        int hp = 0;
        int attack = 0;
        int defense = 0;
        int attackSpeed = 0;
        for (EquipmentSetBonusResponse bonus : bonusesFor(equippedItems)) {
            if (!bonus.isActive()) {
                continue;
            }
            hp += bonus.getHp();
            attack += bonus.getAttack();
            defense += bonus.getDefense();
            attackSpeed += bonus.getAttackSpeed();
        }
        return new CharacterStats(hp, attack, defense, attackSpeed, 0);
    }

    private static Map<String, SetState> collect(List<ItemConfig> equippedItems) {
        Map<String, SetState> states = new LinkedHashMap<String, SetState>();
        for (ItemConfig item : equippedItems) {
            if (item.getSetId() == null || item.getSetId().trim().isEmpty()) {
                continue;
            }
            SetState state = states.get(item.getSetId());
            if (state == null) {
                state = new SetState();
                state.setId = item.getSetId();
                state.setName = item.getSetName();
                states.put(item.getSetId(), state);
            }
            state.pieces++;
            if ((state.bonuses == null || state.bonuses.isEmpty())
                    && item.getSetBonuses() != null && !item.getSetBonuses().isEmpty()) {
                state.bonuses = item.getSetBonuses();
            }
        }
        return states;
    }

    private static class SetState {
        private String setId;
        private String setName;
        private int pieces;
        private List<EquipmentSetBonusConfig> bonuses = new ArrayList<EquipmentSetBonusConfig>();
    }
}
