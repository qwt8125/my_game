package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class SkillConfig {

    private String id;
    private String name;
    private String className;
    private String type;
    private int requiredLevel;
    private int maxLevel;
    private String description;
    private double triggerChance;
    private int cooldownRounds;
    private String targetType;
    private double damageMultiplier;
    private double levelDamageMultiplier;
    private int flatDamage;
    private int levelFlatDamage;
    private double selfHealMultiplier;
    private int dotRounds;
    private int dotDamage;
    private int defenseBreak;
    private int passiveHp;
    private int passiveAttack;
    private int passiveDefense;
    private int passiveAttackSpeed;
    private int upgradeGoldBase;
    private int upgradeGoldStep;
    private List<TaskRewardItemConfig> upgradeMaterials = new ArrayList<TaskRewardItemConfig>();

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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getTriggerChance() { return triggerChance; }
    public void setTriggerChance(double triggerChance) { this.triggerChance = triggerChance; }
    public int getCooldownRounds() { return cooldownRounds; }
    public void setCooldownRounds(int cooldownRounds) { this.cooldownRounds = cooldownRounds; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(double damageMultiplier) { this.damageMultiplier = damageMultiplier; }
    public double getLevelDamageMultiplier() { return levelDamageMultiplier; }
    public void setLevelDamageMultiplier(double levelDamageMultiplier) { this.levelDamageMultiplier = levelDamageMultiplier; }
    public int getFlatDamage() { return flatDamage; }
    public void setFlatDamage(int flatDamage) { this.flatDamage = flatDamage; }
    public int getLevelFlatDamage() { return levelFlatDamage; }
    public void setLevelFlatDamage(int levelFlatDamage) { this.levelFlatDamage = levelFlatDamage; }
    public double getSelfHealMultiplier() { return selfHealMultiplier; }
    public void setSelfHealMultiplier(double selfHealMultiplier) { this.selfHealMultiplier = selfHealMultiplier; }
    public int getDotRounds() { return dotRounds; }
    public void setDotRounds(int dotRounds) { this.dotRounds = dotRounds; }
    public int getDotDamage() { return dotDamage; }
    public void setDotDamage(int dotDamage) { this.dotDamage = dotDamage; }
    public int getDefenseBreak() { return defenseBreak; }
    public void setDefenseBreak(int defenseBreak) { this.defenseBreak = defenseBreak; }
    public int getPassiveHp() { return passiveHp; }
    public void setPassiveHp(int passiveHp) { this.passiveHp = passiveHp; }
    public int getPassiveAttack() { return passiveAttack; }
    public void setPassiveAttack(int passiveAttack) { this.passiveAttack = passiveAttack; }
    public int getPassiveDefense() { return passiveDefense; }
    public void setPassiveDefense(int passiveDefense) { this.passiveDefense = passiveDefense; }
    public int getPassiveAttackSpeed() { return passiveAttackSpeed; }
    public void setPassiveAttackSpeed(int passiveAttackSpeed) { this.passiveAttackSpeed = passiveAttackSpeed; }
    public int getUpgradeGoldBase() { return upgradeGoldBase; }
    public void setUpgradeGoldBase(int upgradeGoldBase) { this.upgradeGoldBase = upgradeGoldBase; }
    public int getUpgradeGoldStep() { return upgradeGoldStep; }
    public void setUpgradeGoldStep(int upgradeGoldStep) { this.upgradeGoldStep = upgradeGoldStep; }
    public List<TaskRewardItemConfig> getUpgradeMaterials() { return upgradeMaterials; }
    public void setUpgradeMaterials(List<TaskRewardItemConfig> upgradeMaterials) {
        this.upgradeMaterials = upgradeMaterials == null ? new ArrayList<TaskRewardItemConfig>() : upgradeMaterials;
    }
}
