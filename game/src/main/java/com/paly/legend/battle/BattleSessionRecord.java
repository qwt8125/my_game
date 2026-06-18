package com.paly.legend.battle;

public class BattleSessionRecord {

    private long id;
    private long characterId;
    private String mapId;
    private String monsterId;
    private String sourceType;
    private String sourceId;
    private int rewardMultiplier;
    private String status;
    private int round;
    private int playerHp;
    private int playerMaxHp;
    private int playerAttack;
    private int playerDefense;
    private int playerAttackSpeed;
    private int monsterHp;
    private int monsterMaxHp;
    private int monsterAttack;
    private int monsterDefense;
    private int monsterAttackSpeed;
    private String nextActor;
    private boolean settled;
    private String actionsJson;
    private String resultJson;
    private String skillStateJson;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public String getMonsterId() { return monsterId; }
    public void setMonsterId(String monsterId) { this.monsterId = monsterId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public int getRewardMultiplier() { return rewardMultiplier; }
    public void setRewardMultiplier(int rewardMultiplier) { this.rewardMultiplier = rewardMultiplier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }
    public int getPlayerHp() { return playerHp; }
    public void setPlayerHp(int playerHp) { this.playerHp = playerHp; }
    public int getPlayerMaxHp() { return playerMaxHp; }
    public void setPlayerMaxHp(int playerMaxHp) { this.playerMaxHp = playerMaxHp; }
    public int getPlayerAttack() { return playerAttack; }
    public void setPlayerAttack(int playerAttack) { this.playerAttack = playerAttack; }
    public int getPlayerDefense() { return playerDefense; }
    public void setPlayerDefense(int playerDefense) { this.playerDefense = playerDefense; }
    public int getPlayerAttackSpeed() { return playerAttackSpeed; }
    public void setPlayerAttackSpeed(int playerAttackSpeed) { this.playerAttackSpeed = playerAttackSpeed; }
    public int getMonsterHp() { return monsterHp; }
    public void setMonsterHp(int monsterHp) { this.monsterHp = monsterHp; }
    public int getMonsterMaxHp() { return monsterMaxHp; }
    public void setMonsterMaxHp(int monsterMaxHp) { this.monsterMaxHp = monsterMaxHp; }
    public int getMonsterAttack() { return monsterAttack; }
    public void setMonsterAttack(int monsterAttack) { this.monsterAttack = monsterAttack; }
    public int getMonsterDefense() { return monsterDefense; }
    public void setMonsterDefense(int monsterDefense) { this.monsterDefense = monsterDefense; }
    public int getMonsterAttackSpeed() { return monsterAttackSpeed; }
    public void setMonsterAttackSpeed(int monsterAttackSpeed) { this.monsterAttackSpeed = monsterAttackSpeed; }
    public String getNextActor() { return nextActor; }
    public void setNextActor(String nextActor) { this.nextActor = nextActor; }
    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }
    public String getActionsJson() { return actionsJson; }
    public void setActionsJson(String actionsJson) { this.actionsJson = actionsJson; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getSkillStateJson() { return skillStateJson; }
    public void setSkillStateJson(String skillStateJson) { this.skillStateJson = skillStateJson; }
}
