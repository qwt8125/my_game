package com.paly.legend.boss;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BossRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String BOSS_COOLDOWN_KEY_PREFIX = "legend:boss:cooldown:";
    private static final Duration AVAILABLE_TTL = Duration.ofHours(24);

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    private final RowMapper<BossStateRecord> mapper = (rs, rowNum) -> {
        BossStateRecord record = new BossStateRecord();
        record.setBossId(rs.getString("boss_id"));
        record.setAvailableAt(LocalDateTime.parse(rs.getString("available_at"), FORMATTER));
        long lastKilledBy = rs.getLong("last_killed_by");
        record.setLastKilledBy(rs.wasNull() ? null : lastKilledBy);
        return record;
    };

    public BossRepository(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    public BossStateRecord findByBossId(String bossId) {
        BossStateRecord cached = findCached(bossId);
        if (cached != null) {
            return cached;
        }
        List<BossStateRecord> records = jdbcTemplate.query(
                "SELECT boss_id, available_at, last_killed_by FROM boss_states WHERE boss_id = ?",
                mapper,
                bossId);
        BossStateRecord record = records.isEmpty() ? null : records.get(0);
        if (record != null) {
            cache(record);
        }
        return record;
    }

    public void ensure(String bossId, LocalDateTime availableAt) {
        if (findByBossId(bossId) == null) {
            jdbcTemplate.update(
                    "INSERT INTO boss_states(boss_id, available_at) VALUES(?, ?)",
                    bossId,
                    FORMATTER.format(availableAt));
            BossStateRecord record = new BossStateRecord();
            record.setBossId(bossId);
            record.setAvailableAt(availableAt);
            cache(record);
        }
    }

    public void markKilled(String bossId, long characterId, LocalDateTime availableAt) {
        jdbcTemplate.update(
                "UPDATE boss_states SET available_at = ?, last_killed_by = ?, updated_at = CURRENT_TIMESTAMP WHERE boss_id = ?",
                FORMATTER.format(availableAt),
                characterId,
                bossId);
        BossStateRecord record = new BossStateRecord();
        record.setBossId(bossId);
        record.setAvailableAt(availableAt);
        record.setLastKilledBy(characterId);
        cache(record);
    }

    private BossStateRecord findCached(String bossId) {
        String value;
        try {
            value = redisTemplate.opsForValue().get(key(bossId));
        } catch (RuntimeException ex) {
            return null;
        }
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String[] parts = value.split("\\|", -1);
        if (parts.length < 2) {
            return null;
        }
        BossStateRecord record = new BossStateRecord();
        record.setBossId(parts[0]);
        record.setAvailableAt(LocalDateTime.parse(parts[1], FORMATTER));
        if (parts.length > 2 && !parts[2].trim().isEmpty()) {
            record.setLastKilledBy(Long.parseLong(parts[2]));
        }
        return record;
    }

    private void cache(BossStateRecord record) {
        String lastKilledBy = record.getLastKilledBy() == null ? "" : String.valueOf(record.getLastKilledBy());
        String value = record.getBossId() + "|" + FORMATTER.format(record.getAvailableAt()) + "|" + lastKilledBy;
        Duration ttl = Duration.between(LocalDateTime.now(), record.getAvailableAt()).plusMinutes(10);
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = AVAILABLE_TTL;
        }
        try {
            redisTemplate.opsForValue().set(key(record.getBossId()), value, ttl);
        } catch (RuntimeException ex) {
            // Boss cooldown is persisted in SQLite. Redis only accelerates reads.
        }
    }

    private String key(String bossId) {
        return BOSS_COOLDOWN_KEY_PREFIX + bossId;
    }
}
