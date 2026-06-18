package com.paly.legend.battle;

public class BattleEnemyResponse {

    private String id;
    private String monsterId;
    private String name;
    private String row;
    private boolean elite;
    private boolean alive;
    private boolean currentTarget;
    private boolean guarded;
    private boolean selectable;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int attackSpeed;

    public static BattleEnemyResponse from(BattleEnemyState state, String currentTargetId, boolean frontAlive) {
        BattleEnemyResponse response = new BattleEnemyResponse();
        response.setId(state.getId());
        response.setMonsterId(state.getMonsterId());
        response.setName(state.getName());
        response.setRow(state.getRow());
        response.setElite(state.isElite());
        response.setAlive(state.isAlive());
        response.setCurrentTarget(state.getId() != null && state.getId().equals(currentTargetId));
        response.setGuarded(state.isAlive() && frontAlive && "back".equals(state.getRow()));
        response.setSelectable(state.isAlive() && !response.isGuarded());
        response.setHp(state.getHp());
        response.setMaxHp(state.getMaxHp());
        response.setAttack(state.getAttack());
        response.setDefense(state.getDefense());
        response.setAttackSpeed(state.getAttackSpeed());
        return response;
    }

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
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public boolean isCurrentTarget() { return currentTarget; }
    public void setCurrentTarget(boolean currentTarget) { this.currentTarget = currentTarget; }
    public boolean isGuarded() { return guarded; }
    public void setGuarded(boolean guarded) { this.guarded = guarded; }
    public boolean isSelectable() { return selectable; }
    public void setSelectable(boolean selectable) { this.selectable = selectable; }
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
}
