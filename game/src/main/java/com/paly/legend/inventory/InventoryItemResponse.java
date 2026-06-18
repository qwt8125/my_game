package com.paly.legend.inventory;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.config.ItemConfig;
import com.paly.legend.equipment.EquipmentAffix;
import com.paly.legend.equipment.EquipmentEnhancement;

public class InventoryItemResponse {

    private long id;
    private String itemId;
    private String name;
    private String type;
    private String slot;
    private String quality;
    private String setId;
    private String setName;
    private int requiredLevel;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int hp;
    private int sellGold;
    private int buffHp;
    private int buffAttack;
    private int buffDefense;
    private int buffAttackSpeed;
    private int quantity;
    private int bindStatus;
    private int enhanceLevel;
    private List<EquipmentAffix> affixes = new ArrayList<EquipmentAffix>();
    private double skillTriggerBonus;

    public static InventoryItemResponse from(InventoryItemRecord record, ItemConfig config) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(record.getId());
        response.setItemId(record.getItemId());
        response.setName(config.getName());
        response.setType(config.getType());
        response.setSlot(config.getSlot());
        response.setQuality(config.getQuality());
        response.setSetId(config.getSetId());
        response.setSetName(config.getSetName());
        response.setRequiredLevel(config.getRequiredLevel());
        EquipmentEnhancement enhancement = EquipmentEnhancement.readFromJson(record.getExtraJson());
        response.setEnhanceLevel(enhancement.getEnhanceLevel());
        response.setAffixes(enhancement.getAffixes());
        response.setSkillTriggerBonus(EquipmentEnhancement.skillTriggerBonus(enhancement));
        response.setAttack(EquipmentEnhancement.attack(config, enhancement));
        response.setDefense(EquipmentEnhancement.defense(config, enhancement));
        response.setAttackSpeed(EquipmentEnhancement.attackSpeed(config, enhancement));
        response.setHp(EquipmentEnhancement.hp(config, enhancement));
        response.setSellGold(config.getSellGold());
        response.setBuffHp(config.getBuffHp());
        response.setBuffAttack(config.getBuffAttack());
        response.setBuffDefense(config.getBuffDefense());
        response.setBuffAttackSpeed(config.getBuffAttackSpeed());
        response.setQuantity(record.getQuantity());
        response.setBindStatus(record.getBindStatus());
        return response;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
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

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getSellGold() {
        return sellGold;
    }

    public void setSellGold(int sellGold) {
        this.sellGold = sellGold;
    }

    public int getBuffHp() {
        return buffHp;
    }

    public void setBuffHp(int buffHp) {
        this.buffHp = buffHp;
    }

    public int getBuffAttack() {
        return buffAttack;
    }

    public void setBuffAttack(int buffAttack) {
        this.buffAttack = buffAttack;
    }

    public int getBuffDefense() {
        return buffDefense;
    }

    public void setBuffDefense(int buffDefense) {
        this.buffDefense = buffDefense;
    }

    public int getBuffAttackSpeed() {
        return buffAttackSpeed;
    }

    public void setBuffAttackSpeed(int buffAttackSpeed) {
        this.buffAttackSpeed = buffAttackSpeed;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(int bindStatus) {
        this.bindStatus = bindStatus;
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
