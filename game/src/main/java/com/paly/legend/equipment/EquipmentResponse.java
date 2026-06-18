package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.List;

public class EquipmentResponse {

    private List<EquipmentItemResponse> items = new ArrayList<EquipmentItemResponse>();
    private List<EquipmentSetBonusResponse> setBonuses = new ArrayList<EquipmentSetBonusResponse>();
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int power;

    public List<EquipmentItemResponse> getItems() {
        return items;
    }

    public void setItems(List<EquipmentItemResponse> items) {
        this.items = items;
    }

    public List<EquipmentSetBonusResponse> getSetBonuses() {
        return setBonuses;
    }

    public void setSetBonuses(List<EquipmentSetBonusResponse> setBonuses) {
        this.setBonuses = setBonuses;
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
