package com.paly.legend.equipment;

public class CharacterStats {

    private final int hp;
    private final int attack;
    private final int defense;
    private final int attackSpeed;
    private final int power;

    public CharacterStats(int hp, int attack, int defense, int attackSpeed, int power) {
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.attackSpeed = attackSpeed;
        this.power = power;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public int getPower() {
        return power;
    }
}
