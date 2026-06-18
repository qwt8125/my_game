package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.config.ItemConfig;

public class EquipmentItemResponse {

    private String slot;
    private long inventoryItemId;
    private String itemId;
    private String name;
    private String quality;
    private String setId;
    private String setName;
    private int requiredLevel;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int enhanceLevel;
    private List<EquipmentAffix> affixes = new ArrayList<EquipmentAffix>();
    private double skillTriggerBonus;

    public static EquipmentItemResponse from(EquipmentRecord record, ItemConfig config) {
        return from(record, config, 0);
    }

    public static EquipmentItemResponse from(EquipmentRecord record, ItemConfig config, int enhanceLevel) {
        EquipmentEnhancement enhancement = new EquipmentEnhancement();
        enhancement.setEnhanceLevel(enhanceLevel);
        return from(record, config, enhancement);
    }

    public static EquipmentItemResponse from(EquipmentRecord record, ItemConfig config, EquipmentEnhancement enhancement) {
        EquipmentItemResponse response = new EquipmentItemResponse();
        response.setSlot(record.getSlot());
        response.setInventoryItemId(record.getInventoryItemId());
        response.setItemId(record.getItemId());
        response.setName(config.getName());
        response.setQuality(config.getQuality());
        response.setSetId(config.getSetId());
        response.setSetName(config.getSetName());
        response.setRequiredLevel(config.getRequiredLevel());
        response.setEnhanceLevel(enhancement == null ? 0 : enhancement.getEnhanceLevel());
        response.setAffixes(enhancement == null ? null : enhancement.getAffixes());
        response.setSkillTriggerBonus(EquipmentEnhancement.skillTriggerBonus(enhancement));
        response.setHp(EquipmentEnhancement.hp(config, enhancement));
        response.setAttack(EquipmentEnhancement.attack(config, enhancement));
        response.setDefense(EquipmentEnhancement.defense(config, enhancement));
        response.setAttackSpeed(EquipmentEnhancement.attackSpeed(config, enhancement));
        return response;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

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

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

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

    public double getSkillTriggerBonus() {
        return skillTriggerBonus;
    }

    public void setSkillTriggerBonus(double skillTriggerBonus) {
        this.skillTriggerBonus = skillTriggerBonus;
    }
}
