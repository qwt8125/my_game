package com.paly.legend.boss;

import com.paly.legend.battle.BattleResponse;

public class BossFightResponse {

    private BattleResponse battle;
    private int bonusExp;
    private int bonusGold;
    private String nextAvailableAt;

    public BattleResponse getBattle() {
        return battle;
    }

    public void setBattle(BattleResponse battle) {
        this.battle = battle;
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

    public String getNextAvailableAt() {
        return nextAvailableAt;
    }

    public void setNextAvailableAt(String nextAvailableAt) {
        this.nextAvailableAt = nextAvailableAt;
    }
}
