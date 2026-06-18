package com.paly.legend.battle;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "game.battle.session-store", havingValue = "redis")
public class RedisBattleSessionStore implements BattleSessionStore {

    private static final String SESSION_KEY_PREFIX = "legend:battle:session:";
    private static final String CHARACTER_RUNNING_KEY_PREFIX = "legend:battle:character:running:";
    private static final String SESSION_ID_KEY = "legend:battle:session:id";
    private static final long TTL_HOURS = 8L;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisBattleSessionStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public long create(long characterId, String mapId, String monsterId, String sourceType, String sourceId,
                       int rewardMultiplier, int playerHp, int playerAttack, int playerDefense,
                       int playerAttackSpeed, int monsterHp, int monsterAttack, int monsterDefense,
                       int monsterAttackSpeed, String nextActor, String actionsJson, String skillStateJson) {
        long battleId = redisTemplate.opsForValue().increment(SESSION_ID_KEY, 1L);
        if (battleId <= 0) {
            throw new IllegalStateException("Failed to allocate battle id");
        }
        BattleSessionRecord record = new BattleSessionRecord();
        record.setId(battleId);
        record.setCharacterId(characterId);
        record.setMapId(mapId);
        record.setMonsterId(monsterId);
        record.setSourceType(sourceType);
        record.setSourceId(sourceId);
        record.setRewardMultiplier(Math.max(1, rewardMultiplier));
        record.setStatus("running");
        record.setRound(1);
        record.setPlayerHp(playerHp);
        record.setPlayerMaxHp(playerHp);
        record.setPlayerAttack(playerAttack);
        record.setPlayerDefense(playerDefense);
        record.setPlayerAttackSpeed(playerAttackSpeed);
        record.setMonsterHp(monsterHp);
        record.setMonsterMaxHp(monsterHp);
        record.setMonsterAttack(monsterAttack);
        record.setMonsterDefense(monsterDefense);
        record.setMonsterAttackSpeed(monsterAttackSpeed);
        record.setNextActor(nextActor);
        record.setSettled(false);
        record.setActionsJson(actionsJson);
        record.setResultJson("{}");
        record.setSkillStateJson(skillStateJson);
        save(record);
        redisTemplate.opsForValue().set(characterRunningKey(characterId), String.valueOf(battleId), Duration.ofHours(TTL_HOURS));
        return battleId;
    }

    @Override
    public BattleSessionRecord findById(long battleId) {
        String json = redisTemplate.opsForValue().get(sessionKey(battleId));
        return parse(json);
    }

    @Override
    public BattleSessionRecord findRunningByCharacterId(long characterId) {
        String battleId = redisTemplate.opsForValue().get(characterRunningKey(characterId));
        if (battleId == null || battleId.trim().isEmpty()) {
            return null;
        }
        return findById(Long.parseLong(battleId));
    }

    @Override
    public void updateRunning(long battleId, int round, int playerHp, int monsterHp, String nextActor, String actionsJson, String skillStateJson) {
        BattleSessionRecord record = require(battleId);
        if (!"running".equals(record.getStatus())) {
            return;
        }
        record.setRound(round);
        record.setPlayerHp(playerHp);
        record.setMonsterHp(monsterHp);
        record.setNextActor(nextActor);
        record.setActionsJson(actionsJson);
        record.setSkillStateJson(skillStateJson);
        save(record);
    }

    @Override
    public void finish(long battleId, int round, int playerHp, int monsterHp,
                       String actionsJson, String skillStateJson, String resultJson) {
        BattleSessionRecord record = require(battleId);
        if (!"running".equals(record.getStatus())) {
            return;
        }
        record.setStatus("finished");
        record.setRound(round);
        record.setPlayerHp(playerHp);
        record.setMonsterHp(monsterHp);
        record.setNextActor("none");
        record.setSettled(true);
        record.setActionsJson(actionsJson);
        record.setSkillStateJson(skillStateJson);
        record.setResultJson(resultJson);
        save(record);
        redisTemplate.delete(characterRunningKey(record.getCharacterId()));
    }

    private BattleSessionRecord require(long battleId) {
        BattleSessionRecord record = findById(battleId);
        if (record == null) {
            throw new IllegalStateException("Battle session not found: " + battleId);
        }
        return record;
    }

    private void save(BattleSessionRecord record) {
        try {
            redisTemplate.opsForValue().set(sessionKey(record.getId()), objectMapper.writeValueAsString(record), Duration.ofHours(TTL_HOURS));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize battle session", ex);
        }
    }

    private BattleSessionRecord parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, BattleSessionRecord.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse battle session", ex);
        }
    }

    private String sessionKey(long battleId) {
        return SESSION_KEY_PREFIX + battleId;
    }

    private String characterRunningKey(long characterId) {
        return CHARACTER_RUNNING_KEY_PREFIX + characterId;
    }
}
