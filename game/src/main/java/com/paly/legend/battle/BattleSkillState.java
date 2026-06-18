package com.paly.legend.battle;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BattleSkillState {

    private Map<String, Integer> cooldowns = new HashMap<String, Integer>();
    private List<BattleEnemyState> enemies = new ArrayList<BattleEnemyState>();
    private String dotTargetId;
    private int dotRounds;
    private int dotDamage;
    private String dotName;

    public Map<String, Integer> getCooldowns() {
        return cooldowns;
    }

    public void setCooldowns(Map<String, Integer> cooldowns) {
        this.cooldowns = cooldowns == null ? new HashMap<String, Integer>() : cooldowns;
    }

    public List<BattleEnemyState> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<BattleEnemyState> enemies) {
        this.enemies = enemies == null ? new ArrayList<BattleEnemyState>() : enemies;
    }

    public String getDotTargetId() {
        return dotTargetId;
    }

    public void setDotTargetId(String dotTargetId) {
        this.dotTargetId = dotTargetId;
    }

    public int getDotRounds() {
        return dotRounds;
    }

    public void setDotRounds(int dotRounds) {
        this.dotRounds = dotRounds;
    }

    public int getDotDamage() {
        return dotDamage;
    }

    public void setDotDamage(int dotDamage) {
        this.dotDamage = dotDamage;
    }

    public String getDotName() {
        return dotName;
    }

    public void setDotName(String dotName) {
        this.dotName = dotName;
    }
}
