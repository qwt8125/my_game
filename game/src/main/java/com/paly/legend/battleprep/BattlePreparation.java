package com.paly.legend.battleprep;

public class BattlePreparation {

    private long characterId;
    private int bonusHp;
    private int bonusAttack;
    private int bonusDefense;
    private int bonusAttackSpeed;

    public long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
    }

    public int getBonusHp() {
        return bonusHp;
    }

    public void setBonusHp(int bonusHp) {
        this.bonusHp = bonusHp;
    }

    public int getBonusAttack() {
        return bonusAttack;
    }

    public void setBonusAttack(int bonusAttack) {
        this.bonusAttack = bonusAttack;
    }

    public int getBonusDefense() {
        return bonusDefense;
    }

    public void setBonusDefense(int bonusDefense) {
        this.bonusDefense = bonusDefense;
    }

    public int getBonusAttackSpeed() {
        return bonusAttackSpeed;
    }

    public void setBonusAttackSpeed(int bonusAttackSpeed) {
        this.bonusAttackSpeed = bonusAttackSpeed;
    }

    public boolean hasBonus() {
        return bonusHp > 0 || bonusAttack > 0 || bonusDefense > 0 || bonusAttackSpeed > 0;
    }
}
