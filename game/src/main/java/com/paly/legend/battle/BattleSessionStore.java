package com.paly.legend.battle;

public interface BattleSessionStore {

    long create(long characterId, String mapId, String monsterId,
                String sourceType, String sourceId, int rewardMultiplier,
                int playerHp, int playerAttack, int playerDefense,
                int playerAttackSpeed, int monsterHp, int monsterAttack,
                int monsterDefense, int monsterAttackSpeed,
                String nextActor, String actionsJson, String skillStateJson);

    BattleSessionRecord findById(long battleId);

    BattleSessionRecord findRunningByCharacterId(long characterId);

    void updateRunning(long battleId, int round, int playerHp, int monsterHp,
                       String nextActor, String actionsJson, String skillStateJson);

    void finish(long battleId, int round, int playerHp, int monsterHp,
                String actionsJson, String skillStateJson, String resultJson);
}
