package com.paly.legend.battle;

import java.util.ArrayList;
import java.util.List;

public class BattleSessionResponse {

    private long battleId;
    private String status;
    private Boolean win;
    private int round;
    private String nextActor;
    private int suggestedDelayMs;
    private BattleParticipantResponse player;
    private BattleParticipantResponse monster;
    private List<BattleEnemyResponse> enemies = new ArrayList<BattleEnemyResponse>();
    private BattleActionResponse action;
    private List<BattleActionResponse> actions = new ArrayList<BattleActionResponse>();
    private int expGained;
    private int goldGained;
    private int bonusExp;
    private int bonusGold;
    private int levelBefore;
    private int levelAfter;
    private int levelUps;
    private int currentExp;
    private int currentGold;
    private int power;
    private long battleLogId;
    private List<BattleDropResponse> drops = new ArrayList<BattleDropResponse>();
    private List<BattleSkillOptionResponse> skills = new ArrayList<BattleSkillOptionResponse>();

    public long getBattleId() {
        return battleId;
    }

    public void setBattleId(long battleId) {
        this.battleId = battleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public String getNextActor() {
        return nextActor;
    }

    public void setNextActor(String nextActor) {
        this.nextActor = nextActor;
    }

    public int getSuggestedDelayMs() {
        return suggestedDelayMs;
    }

    public void setSuggestedDelayMs(int suggestedDelayMs) {
        this.suggestedDelayMs = suggestedDelayMs;
    }

    public BattleParticipantResponse getPlayer() {
        return player;
    }

    public void setPlayer(BattleParticipantResponse player) {
        this.player = player;
    }

    public BattleParticipantResponse getMonster() {
        return monster;
    }

    public void setMonster(BattleParticipantResponse monster) {
        this.monster = monster;
    }

    public List<BattleEnemyResponse> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<BattleEnemyResponse> enemies) {
        this.enemies = enemies == null ? new ArrayList<BattleEnemyResponse>() : enemies;
    }

    public BattleActionResponse getAction() {
        return action;
    }

    public void setAction(BattleActionResponse action) {
        this.action = action;
    }

    public List<BattleActionResponse> getActions() {
        return actions;
    }

    public void setActions(List<BattleActionResponse> actions) {
        this.actions = actions;
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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public long getBattleLogId() {
        return battleLogId;
    }

    public void setBattleLogId(long battleLogId) {
        this.battleLogId = battleLogId;
    }

    public List<BattleDropResponse> getDrops() {
        return drops;
    }

    public void setDrops(List<BattleDropResponse> drops) {
        this.drops = drops;
    }

    public List<BattleSkillOptionResponse> getSkills() {
        return skills;
    }

    public void setSkills(List<BattleSkillOptionResponse> skills) {
        this.skills = skills;
    }
}
