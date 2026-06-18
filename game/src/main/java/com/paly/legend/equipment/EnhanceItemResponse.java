package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.List;

public class EnhanceItemResponse {

    private long inventoryItemId;
    private int enhanceLevel;
    private int goldCost;
    private List<EnhancementMaterialCost> materialCosts = new ArrayList<EnhancementMaterialCost>();
    private int currentGold;
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

    public int getEnhanceLevel() {
        return enhanceLevel;
    }

    public void setEnhanceLevel(int enhanceLevel) {
        this.enhanceLevel = enhanceLevel;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public List<EnhancementMaterialCost> getMaterialCosts() {
        return materialCosts;
    }

    public void setMaterialCosts(List<EnhancementMaterialCost> materialCosts) {
        this.materialCosts = materialCosts;
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public void setCurrentGold(int currentGold) {
        this.currentGold = currentGold;
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
