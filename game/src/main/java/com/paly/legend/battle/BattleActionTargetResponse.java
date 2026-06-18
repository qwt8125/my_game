package com.paly.legend.battle;

public class BattleActionTargetResponse {

    private String targetId;
    private String name;
    private int damage;
    private int hpAfter;

    public BattleActionTargetResponse() {
    }

    public BattleActionTargetResponse(String targetId, String name, int damage, int hpAfter) {
        this.targetId = targetId;
        this.name = name;
        this.damage = damage;
        this.hpAfter = hpAfter;
    }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getHpAfter() { return hpAfter; }
    public void setHpAfter(int hpAfter) { this.hpAfter = hpAfter; }
}
