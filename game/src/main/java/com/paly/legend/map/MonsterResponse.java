package com.paly.legend.map;

import com.paly.legend.config.MonsterConfig;

public class MonsterResponse {

    private String id;
    private String name;
    private int level;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int exp;
    private int goldMin;
    private int goldMax;

    public static MonsterResponse from(MonsterConfig monster) {
        MonsterResponse response = new MonsterResponse();
        response.setId(monster.getId());
        response.setName(monster.getName());
        response.setLevel(monster.getLevel());
        response.setHp(monster.getHp());
        response.setAttack(monster.getAttack());
        response.setDefense(monster.getDefense());
        response.setAttackSpeed(monster.getAttackSpeed());
        response.setExp(monster.getExp());
        response.setGoldMin(monster.getGoldMin());
        response.setGoldMax(monster.getGoldMax());
        return response;
    }

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
