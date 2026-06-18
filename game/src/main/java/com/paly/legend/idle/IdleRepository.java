package com.paly.legend.idle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IdleRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<IdleSessionRecord> mapper = (rs, rowNum) -> {
        IdleSessionRecord record = new IdleSessionRecord();
        record.setId(rs.getLong("id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setMapId(rs.getString("map_id"));
        record.setMonsterId(rs.getString("monster_id"));
        record.setStartedAt(LocalDateTime.parse(rs.getString("started_at"), FORMATTER));
        record.setLastClaimedAt(LocalDateTime.parse(rs.getString("last_claimed_at"), FORMATTER));
        return record;
    };

    public IdleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public IdleSessionRecord findByCharacterId(long characterId) {
        List<IdleSessionRecord> records = jdbcTemplate.query(
                "SELECT id, character_id, map_id, monster_id, started_at, last_claimed_at FROM idle_sessions WHERE character_id = ?",
                mapper,
                characterId);
        return records.isEmpty() ? null : records.get(0);
    }

    public void upsert(long characterId, String mapId, String monsterId, LocalDateTime now) {
        if (findByCharacterId(characterId) == null) {
            jdbcTemplate.update(
                    "INSERT INTO idle_sessions(character_id, map_id, monster_id, started_at, last_claimed_at) VALUES(?, ?, ?, ?, ?)",
                    characterId,
                    mapId,
                    monsterId,
                    FORMATTER.format(now),
                    FORMATTER.format(now));
        } else {
            jdbcTemplate.update(
                    "UPDATE idle_sessions SET map_id = ?, monster_id = ?, started_at = ?, last_claimed_at = ?, updated_at = CURRENT_TIMESTAMP WHERE character_id = ?",
                    mapId,
                    monsterId,
                    FORMATTER.format(now),
                    FORMATTER.format(now),
                    characterId);
        }
    }

    public void updateLastClaimedAt(long characterId, LocalDateTime claimedAt) {
        jdbcTemplate.update(
                "UPDATE idle_sessions SET last_claimed_at = ?, updated_at = CURRENT_TIMESTAMP WHERE character_id = ?",
                FORMATTER.format(claimedAt),
                characterId);
    }
}
