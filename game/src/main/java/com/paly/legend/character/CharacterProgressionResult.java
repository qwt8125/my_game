package com.paly.legend.character;

public class CharacterProgressionResult {

    private final int level;
    private final int exp;
    private final int gold;
    private final int hp;
    private final int attack;
    private final int defense;
    private final int attackSpeed;
    private final int power;
    private final int levelUps;

    public CharacterProgressionResult(int level, int exp, int gold, int hp,
                                      int attack, int defense, int attackSpeed, int power, int levelUps) {
        this.level = level;
        this.exp = exp;
        this.gold = gold;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.attackSpeed = attackSpeed;
        this.power = power;
        this.levelUps = levelUps;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public int getGold() {
        return gold;
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

    public int getLevelUps() {
        return levelUps;
    }
}
