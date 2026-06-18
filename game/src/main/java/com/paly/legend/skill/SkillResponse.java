package com.paly.legend.skill;

import com.paly.legend.config.SkillConfig;
import com.paly.legend.task.TaskRewardItemResponse;

import java.util.ArrayList;
import java.util.List;

public class SkillResponse {

    private String id;
    private String name;
    private String className;
    private String type;
    private int requiredLevel;
    private int maxLevel;
    private int level;
    private String description;
    private boolean learned;
    private boolean canLearn;
    private boolean canUpgrade;
    private int upgradeGold;
    private int skillSlot;
    private List<TaskRewardItemResponse> materialCosts = new ArrayList<TaskRewardItemResponse>();
    private String targetType;
    private double triggerChance;
    private int cooldownRounds;
    private double damageMultiplier;
    private int flatDamage;
    private int dotRounds;
    private int dotDamage;
    private int passiveHp;
    private int passiveAttack;
    private int passiveDefense;
    private int passiveAttackSpeed;

    public static SkillResponse from(SkillConfig config, int level, boolean canLearn, boolean canUpgrade, int upgradeGold) {
        SkillResponse response = new SkillResponse();
        response.setId(config.getId());
        response.setName(config.getName());
        response.setClassName(config.getClassName());
        response.setType(config.getType());
        response.setRequiredLevel(config.getRequiredLevel());
        response.setMaxLevel(config.getMaxLevel());
        response.setLevel(level);
        response.setLearned(level > 0);
        response.setDescription(config.getDescription());
        response.setCanLearn(canLearn);
        response.setCanUpgrade(canUpgrade);
        response.setUpgradeGold(upgradeGold);
        response.setTargetType(config.getTargetType());
        response.setTriggerChance(config.getTriggerChance());
        response.setCooldownRounds(config.getCooldownRounds());
        response.setDamageMultiplier(config.getDamageMultiplier() + Math.max(0, level - 1) * config.getLevelDamageMultiplier());
        response.setFlatDamage(config.getFlatDamage() + Math.max(0, level - 1) * config.getLevelFlatDamage());
        response.setDotRounds(config.getDotRounds());
        response.setDotDamage(config.getDotDamage() * Math.max(1, level));
        response.setPassiveHp(config.getPassiveHp() * Math.max(1, level));
        response.setPassiveAttack(config.getPassiveAttack() * Math.max(1, level));
        response.setPassiveDefense(config.getPassiveDefense() * Math.max(1, level));
        response.setPassiveAttackSpeed(config.getPassiveAttackSpeed() * Math.max(1, level));
        return response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isLearned() { return learned; }
    public void setLearned(boolean learned) { this.learned = learned; }
    public boolean isCanLearn() { return canLearn; }
    public void setCanLearn(boolean canLearn) { this.canLearn = canLearn; }
    public boolean isCanUpgrade() { return canUpgrade; }
    public void setCanUpgrade(boolean canUpgrade) { this.canUpgrade = canUpgrade; }
    public int getUpgradeGold() { return upgradeGold; }
    public void setUpgradeGold(int upgradeGold) { this.upgradeGold = upgradeGold; }
    public int getSkillSlot() { return skillSlot; }
    public void setSkillSlot(int skillSlot) { this.skillSlot = skillSlot; }
    public List<TaskRewardItemResponse> getMaterialCosts() { return materialCosts; }
    public void setMaterialCosts(List<TaskRewardItemResponse> materialCosts) {
        this.materialCosts = materialCosts == null ? new ArrayList<TaskRewardItemResponse>() : materialCosts;
    }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public double getTriggerChance() { return triggerChance; }
    public void setTriggerChance(double triggerChance) { this.triggerChance = triggerChance; }
    public int getCooldownRounds() { return cooldownRounds; }
    public void setCooldownRounds(int cooldownRounds) { this.cooldownRounds = cooldownRounds; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(double damageMultiplier) { this.damageMultiplier = damageMultiplier; }
    public int getFlatDamage() { return flatDamage; }
    public void setFlatDamage(int flatDamage) { this.flatDamage = flatDamage; }
    public int getDotRounds() { return dotRounds; }
    public void setDotRounds(int dotRounds) { this.dotRounds = dotRounds; }
    public int getDotDamage() { return dotDamage; }
    public void setDotDamage(int dotDamage) { this.dotDamage = dotDamage; }
    public int getPassiveHp() { return passiveHp; }
    public void setPassiveHp(int passiveHp) { this.passiveHp = passiveHp; }
    public int getPassiveAttack() { return passiveAttack; }
    public void setPassiveAttack(int passiveAttack) { this.passiveAttack = passiveAttack; }
    public int getPassiveDefense() { return passiveDefense; }
    public void setPassiveDefense(int passiveDefense) { this.passiveDefense = passiveDefense; }
    public int getPassiveAttackSpeed() { return passiveAttackSpeed; }
    public void setPassiveAttackSpeed(int passiveAttackSpeed) { this.passiveAttackSpeed = passiveAttackSpeed; }
}
