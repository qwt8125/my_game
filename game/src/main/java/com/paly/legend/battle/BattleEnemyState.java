package com.paly.legend.battle;

public class BattleEnemyState {

    private String id;
    private String monsterId;
    private String name;
    private String row;
    private boolean elite;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int attackSpeed;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMonsterId() { return monsterId; }
    public void setMonsterId(String monsterId) { this.monsterId = monsterId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }
    public boolean isElite() { return elite; }
    public void setElite(boolean elite) { this.elite = elite; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }

    public boolean isAlive() {
        return hp > 0;
    }
}
