package com.paly.legend.talent;

import com.paly.legend.config.TalentConfig;

public class TalentResponse {

    private String id;
    private String name;
    private String branch;
    private String description;
    private int level;
    private int maxLevel;
    private int requiredLevel;
    private String preTalentId;
    private int preTalentLevel;
    private boolean canUpgrade;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;
    private double skillTriggerBonus;
    private int goldBonusPercent;

    public static TalentResponse from(TalentConfig config, int level, boolean canUpgrade) {
        TalentResponse response = new TalentResponse();
        response.setId(config.getId());
        response.setName(config.getName());
        response.setBranch(config.getBranch());
        response.setDescription(config.getDescription());
        response.setLevel(level);
        response.setMaxLevel(config.getMaxLevel());
        response.setRequiredLevel(config.getRequiredLevel());
        response.setPreTalentId(config.getPreTalentId());
        response.setPreTalentLevel(config.getPreTalentLevel());
        response.setCanUpgrade(canUpgrade);
        response.setHp(config.getHp());
        response.setAttack(config.getAttack());
        response.setDefense(config.getDefense());
        response.setAttackSpeed(config.getAttackSpeed());
        response.setSkillTriggerBonus(config.getSkillTriggerBonus());
        response.setGoldBonusPercent(config.getGoldBonusPercent());
        return response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    public String getPreTalentId() { return preTalentId; }
    public void setPreTalentId(String preTalentId) { this.preTalentId = preTalentId; }
    public int getPreTalentLevel() { return preTalentLevel; }
    public void setPreTalentLevel(int preTalentLevel) { this.preTalentLevel = preTalentLevel; }
    public boolean isCanUpgrade() { return canUpgrade; }
    public void setCanUpgrade(boolean canUpgrade) { this.canUpgrade = canUpgrade; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }
    public double getSkillTriggerBonus() { return skillTriggerBonus; }
    public void setSkillTriggerBonus(double skillTriggerBonus) { this.skillTriggerBonus = skillTriggerBonus; }
    public int getGoldBonusPercent() { return goldBonusPercent; }
    public void setGoldBonusPercent(int goldBonusPercent) { this.goldBonusPercent = goldBonusPercent; }
}
