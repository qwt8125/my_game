package com.paly.legend.worldboss;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class WorldBossRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<WorldBossStateRecord> stateMapper = (rs, rowNum) -> {
        WorldBossStateRecord record = new WorldBossStateRecord();
        record.setBossId(rs.getString("boss_id"));
        record.setStatus(rs.getString("status"));
        record.setCurrentHp(rs.getInt("current_hp"));
        record.setMaxHp(rs.getInt("max_hp"));
        record.setAvailableAt(LocalDateTime.parse(rs.getString("available_at"), FORMATTER));
        String killedAt = rs.getString("killed_at");
        if (killedAt != null && !killedAt.trim().isEmpty()) {
            record.setKilledAt(LocalDateTime.parse(killedAt, FORMATTER));
        }
        record.setRewardsSent(rs.getInt("rewards_sent") == 1);
        return record;
    };

    public WorldBossRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WorldBossStateRecord findByBossId(String bossId) {
        List<WorldBossStateRecord> records = jdbcTemplate.query(
                "SELECT boss_id, status, current_hp, max_hp, available_at, killed_at, rewards_sent "
                        + "FROM world_boss_states WHERE boss_id = ?",
                stateMapper,
                bossId);
        return records.isEmpty() ? null : records.get(0);
    }

    public WorldBossStateRecord ensure(String bossId, int maxHp, LocalDateTime now) {
        WorldBossStateRecord state = findByBossId(bossId);
        if (state == null) {
            jdbcTemplate.update(
                    "INSERT INTO world_boss_states(boss_id, status, current_hp, max_hp, available_at, rewards_sent) "
                            + "VALUES(?, 'available', ?, ?, ?, 0)",
                    bossId,
                    maxHp,
                    maxHp,
                    FORMATTER.format(now));
            return findByBossId(bossId);
        }

        if ("killed".equals(state.getStatus()) && !state.getAvailableAt().isAfter(now)) {
            jdbcTemplate.update(
                    "UPDATE world_boss_states SET status = 'available', current_hp = ?, max_hp = ?, "
                            + "available_at = ?, killed_at = NULL, rewards_sent = 0, updated_at = CURRENT_TIMESTAMP "
                            + "WHERE boss_id = ?",
                    maxHp,
                    maxHp,
                    FORMATTER.format(now),
                    bossId);
            jdbcTemplate.update("DELETE FROM world_boss_damage_logs WHERE boss_id = ?", bossId);
            return findByBossId(bossId);
        }

        if ("available".equals(state.getStatus()) && state.getMaxHp() != maxHp) {
            jdbcTemplate.update(
                    "UPDATE world_boss_states SET current_hp = ?, max_hp = ?, updated_at = CURRENT_TIMESTAMP WHERE boss_id = ?",
                    maxHp,
                    maxHp,
                    bossId);
            return findByBossId(bossId);
        }

        return state;
    }

    public long recordDamage(String bossId, long characterId, int damage, long battleId) {
        jdbcTemplate.update(
                "INSERT INTO world_boss_damage_logs(boss_id, character_id, damage, battle_id, rewarded) VALUES(?, ?, ?, ?, 0)",
                bossId,
                characterId,
                Math.max(0, damage),
                battleId);
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
        return id == null ? 0 : id;
    }

    public WorldBossStateRecord applyDamage(String bossId, int damage, LocalDateTime killedAt, LocalDateTime nextAvailableAt) {
        WorldBossStateRecord state = findByBossId(bossId);
        if (state == null || !"available".equals(state.getStatus()) || state.getCurrentHp() <= 0) {
            return state;
        }
        int appliedDamage = Math.min(state.getCurrentHp(), Math.max(0, damage));
        int currentHp = Math.max(0, state.getCurrentHp() - appliedDamage);
        if (currentHp <= 0) {
            jdbcTemplate.update(
                    "UPDATE world_boss_states SET status = 'killed', current_hp = 0, killed_at = ?, "
                            + "available_at = ?, updated_at = CURRENT_TIMESTAMP WHERE boss_id = ?",
                    FORMATTER.format(killedAt),
                    FORMATTER.format(nextAvailableAt),
                    bossId);
        } else {
            jdbcTemplate.update(
                    "UPDATE world_boss_states SET current_hp = ?, updated_at = CURRENT_TIMESTAMP WHERE boss_id = ?",
                    currentHp,
                    bossId);
        }
        return findByBossId(bossId);
    }

    public boolean markRewardsSent(String bossId) {
        int updated = jdbcTemplate.update(
                "UPDATE world_boss_states SET rewards_sent = 1, updated_at = CURRENT_TIMESTAMP "
                        + "WHERE boss_id = ? AND rewards_sent = 0",
                bossId);
        if (updated > 0) {
            jdbcTemplate.update(
                    "UPDATE world_boss_damage_logs SET rewarded = 1 WHERE boss_id = ?",
                    bossId);
            return true;
        }
        return false;
    }

    public List<WorldBossDamageRankResponse> listRanks(String bossId, int limit) {
        List<WorldBossDamageRankResponse> rows = jdbcTemplate.query(
                "SELECT c.id AS character_id, c.nickname AS nickname, SUM(l.damage) AS damage, MAX(l.rewarded) AS rewarded "
                        + "FROM world_boss_damage_logs l "
                        + "JOIN characters c ON c.id = l.character_id "
                        + "WHERE l.boss_id = ? "
                        + "GROUP BY c.id, c.nickname "
                        + "ORDER BY damage DESC, c.id ASC "
                        + "LIMIT ?",
                (rs, rowNum) -> {
                    WorldBossDamageRankResponse response = new WorldBossDamageRankResponse();
                    response.setCharacterId(rs.getLong("character_id"));
                    response.setNickname(rs.getString("nickname"));
                    response.setDamage(rs.getInt("damage"));
                    response.setRewarded(rs.getInt("rewarded") == 1);
                    return response;
                },
                bossId,
                Math.max(1, limit));
        List<WorldBossDamageRankResponse> result = new ArrayList<WorldBossDamageRankResponse>();
        for (int i = 0; i < rows.size(); i++) {
            WorldBossDamageRankResponse row = rows.get(i);
            row.setRank(i + 1);
            result.add(row);
        }
        return result;
    }
}
