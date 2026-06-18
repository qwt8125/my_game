package com.paly.legend.battle;

public class BattleSkillOptionResponse {

    private String id;
    private String name;
    private int level;
    private int skillSlot;
    private int cooldownRemaining;
    private boolean ready;
    private String targetType;
    private String description;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getSkillSlot() { return skillSlot; }
    public void setSkillSlot(int skillSlot) { this.skillSlot = skillSlot; }
    public int getCooldownRemaining() { return cooldownRemaining; }
    public void setCooldownRemaining(int cooldownRemaining) { this.cooldownRemaining = cooldownRemaining; }
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
