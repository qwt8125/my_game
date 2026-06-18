package com.paly.legend.battle;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "game.battle.session-store", havingValue = "sqlite", matchIfMissing = true)
public class BattleSessionRepository implements BattleSessionStore {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<BattleSessionRecord> mapper = (rs, rowNum) -> {
        BattleSessionRecord record = new BattleSessionRecord();
        record.setId(rs.getLong("id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setMapId(rs.getString("map_id"));
        record.setMonsterId(rs.getString("monster_id"));
        record.setSourceType(rs.getString("source_type"));
        record.setSourceId(rs.getString("source_id"));
        record.setRewardMultiplier(rs.getInt("reward_multiplier"));
        record.setStatus(rs.getString("status"));
        record.setRound(rs.getInt("round"));
        record.setPlayerHp(rs.getInt("player_hp"));
        record.setPlayerMaxHp(rs.getInt("player_max_hp"));
        record.setPlayerAttack(rs.getInt("player_attack"));
        record.setPlayerDefense(rs.getInt("player_defense"));
        record.setPlayerAttackSpeed(rs.getInt("player_attack_speed"));
        record.setMonsterHp(rs.getInt("monster_hp"));
        record.setMonsterMaxHp(rs.getInt("monster_max_hp"));
        record.setMonsterAttack(rs.getInt("monster_attack"));
        record.setMonsterDefense(rs.getInt("monster_defense"));
        record.setMonsterAttackSpeed(rs.getInt("monster_attack_speed"));
        record.setNextActor(rs.getString("next_actor"));
        record.setSettled(rs.getInt("settled") == 1);
        record.setActionsJson(rs.getString("actions_json"));
        record.setResultJson(rs.getString("result_json"));
        record.setSkillStateJson(rs.getString("skill_state_json"));
        return record;
    };

    public BattleSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long create(final long characterId, final String mapId, final String monsterId,
                       final String sourceType, final String sourceId, final int rewardMultiplier,
                       final int playerHp, final int playerAttack, final int playerDefense,
                       final int playerAttackSpeed, final int monsterHp, final int monsterAttack,
                       final int monsterDefense, final int monsterAttackSpeed,
                       final String nextActor, final String actionsJson, final String skillStateJson) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO battle_sessions(character_id, map_id, monster_id, source_type, source_id, reward_multiplier, status, round, player_hp, player_max_hp, player_attack, player_defense, player_attack_speed, monster_hp, monster_max_hp, monster_attack, monster_defense, monster_attack_speed, next_actor, actions_json, skill_state_json) "
                            + "VALUES(?, ?, ?, ?, ?, ?, 'running', 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, characterId);
            ps.setString(2, mapId);
            ps.setString(3, monsterId);
            ps.setString(4, sourceType);
            ps.setString(5, sourceId);
            ps.setInt(6, Math.max(1, rewardMultiplier));
            ps.setInt(7, playerHp);
            ps.setInt(8, playerHp);
            ps.setInt(9, playerAttack);
            ps.setInt(10, playerDefense);
            ps.setInt(11, playerAttackSpeed);
            ps.setInt(12, monsterHp);
            ps.setInt(13, monsterHp);
            ps.setInt(14, monsterAttack);
            ps.setInt(15, monsterDefense);
            ps.setInt(16, monsterAttackSpeed);
            ps.setString(17, nextActor);
            ps.setString(18, actionsJson);
            ps.setString(19, skillStateJson);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public BattleSessionRecord findById(long battleId) {
        List<BattleSessionRecord> records = jdbcTemplate.query(
                "SELECT * FROM battle_sessions WHERE id = ?",
                mapper,
                battleId);
        return records.isEmpty() ? null : records.get(0);
    }

    @Override
    public BattleSessionRecord findRunningByCharacterId(long characterId) {
        List<BattleSessionRecord> records = jdbcTemplate.query(
                "SELECT * FROM battle_sessions WHERE character_id = ? AND status = 'running' ORDER BY id DESC LIMIT 1",
                mapper,
                characterId);
        return records.isEmpty() ? null : records.get(0);
    }

    @Override
    public void updateRunning(long battleId, int round, int playerHp, int monsterHp,
                              String nextActor, String actionsJson, String skillStateJson) {
        jdbcTemplate.update(
                "UPDATE battle_sessions SET round = ?, player_hp = ?, monster_hp = ?, next_actor = ?, actions_json = ?, skill_state_json = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'running'",
                round,
                playerHp,
                monsterHp,
                nextActor,
                actionsJson,
                skillStateJson,
                battleId);
    }

    @Override
    public void finish(long battleId, int round, int playerHp, int monsterHp,
                       String actionsJson, String skillStateJson, String resultJson) {
        jdbcTemplate.update(
                "UPDATE battle_sessions SET status = 'finished', round = ?, player_hp = ?, monster_hp = ?, next_actor = 'none', settled = 1, actions_json = ?, skill_state_json = ?, result_json = ?, updated_at = CURRENT_TIMESTAMP, finished_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'running'",
                round,
                playerHp,
                monsterHp,
                actionsJson,
                skillStateJson,
                resultJson,
                battleId);
    }
}
