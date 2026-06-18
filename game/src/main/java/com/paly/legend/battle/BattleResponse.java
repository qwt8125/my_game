package com.paly.legend.battle;

import java.util.ArrayList;
import java.util.List;

public class BattleResponse {

    private boolean win;
    private int rounds;
    private int expGained;
    private int goldGained;
    private int bonusExp;
    private int bonusGold;
    private long battleLogId;
    private int levelBefore;
    private int levelAfter;
    private int levelUps;
    private int currentExp;
    private int currentGold;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int power;
    private List<BattleActionResponse> actions = new ArrayList<BattleActionResponse>();
    private List<BattleDropResponse> drops = new ArrayList<BattleDropResponse>();

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getExpGained() {
        return expGained;
    }

    public void setExpGained(int expGained) {
        this.expGained = expGained;
    }

    public int getGoldGained() {
        return goldGained;
    }

    public void setGoldGained(int goldGained) {
        this.goldGained = goldGained;
    }

    public int getBonusExp() {
        return bonusExp;
    }

    public void setBonusExp(int bonusExp) {
        this.bonusExp = bonusExp;
    }

    public int getBonusGold() {
        return bonusGold;
    }

    public void setBonusGold(int bonusGold) {
        this.bonusGold = bonusGold;
    }

    public long getBattleLogId() {
        return battleLogId;
    }

    public void setBattleLogId(long battleLogId) {
        this.battleLogId = battleLogId;
    }

    public int getLevelBefore() {
        return levelBefore;
    }

    public void setLevelBefore(int levelBefore) {
        this.levelBefore = levelBefore;
    }

    public int getLevelAfter() {
        return levelAfter;
    }

    public void setLevelAfter(int levelAfter) {
        this.levelAfter = levelAfter;
    }

    public int getLevelUps() {
        return levelUps;
    }

    public void setLevelUps(int levelUps) {
        this.levelUps = levelUps;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(int currentExp) {
        this.currentExp = currentExp;
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public void setCurrentGold(int currentGold) {
        this.currentGold = currentGold;
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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public List<BattleActionResponse> getActions() {
        return actions;
    }

    public void setActions(List<BattleActionResponse> actions) {
        this.actions = actions;
    }

    public List<BattleDropResponse> getDrops() {
        return drops;
    }

    public void setDrops(List<BattleDropResponse> drops) {
        this.drops = drops;
    }
}
