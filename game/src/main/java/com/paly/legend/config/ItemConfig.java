package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class ItemConfig {

    private String id;
    private String name;
    private String type;
    private String slot;
    private String quality;
    private String setId;
    private String setName;
    private List<EquipmentSetBonusConfig> setBonuses = new ArrayList<EquipmentSetBonusConfig>();
    private int requiredLevel;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int hp;
    private int sellGold;
    private int buffAttack;
    private int buffDefense;
    private int buffAttackSpeed;
    private int buffHp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<EquipmentSetBonusConfig> getSetBonuses() {
        return setBonuses;
    }

    public void setSetBonuses(List<EquipmentSetBonusConfig> setBonuses) {
        this.setBonuses = setBonuses;
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

    public int getBuffHp() {
        return buffHp;
    }

    public void setBuffHp(int buffHp) {
        this.buffHp = buffHp;
    }
}
