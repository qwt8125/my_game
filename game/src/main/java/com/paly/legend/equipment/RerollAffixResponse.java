package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.List;

public class RerollAffixResponse {

    private long inventoryItemId;
    private EnhancementMaterialCost materialCost;
    private List<EquipmentAffix> affixes = new ArrayList<EquipmentAffix>();
    private double skillTriggerBonus;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int power;

    public long getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public EnhancementMaterialCost getMaterialCost() {
        return materialCost;
    }

    public void setMaterialCost(EnhancementMaterialCost materialCost) {
        this.materialCost = materialCost;
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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }
}
