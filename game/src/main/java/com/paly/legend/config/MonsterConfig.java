package com.paly.legend.config;

public class MonsterConfig {

    private String id;
    private String name;
    private int level;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed = 100;
    private int exp;
    private int goldMin;
    private int goldMax;

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getGoldMin() {
        return goldMin;
    }

    public void setGoldMin(int goldMin) {
        this.goldMin = goldMin;
    }

    public int getGoldMax() {
        return goldMax;
    }

    public void setGoldMax(int goldMax) {
        this.goldMax = goldMax;
    }
}
