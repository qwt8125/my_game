package com.paly.legend.map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MapEventStateRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<MapEventStateRecord> mapper = (rs, rowNum) -> {
        MapEventStateRecord record = new MapEventStateRecord();
        record.setId(rs.getLong("id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setEventId(rs.getString("event_id"));
        record.setTriggerCount(rs.getInt("trigger_count"));
        record.setLastTriggeredAt(parse(rs.getString("last_triggered_at")));
        record.setNextAvailableAt(parse(rs.getString("next_available_at")));
        record.setCompleted(rs.getInt("completed") == 1);
        return record;
    };

    public MapEventStateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MapEventStateRecord find(long characterId, String eventId) {
        List<MapEventStateRecord> records = jdbcTemplate.query(
                "SELECT id, character_id, event_id, trigger_count, last_triggered_at, next_available_at, completed "
                        + "FROM map_event_states WHERE character_id = ? AND event_id = ?",
                mapper,
                characterId,
                eventId);
        return records.isEmpty() ? null : records.get(0);
    }

    public List<MapEventStateRecord> findByCharacterId(long characterId) {
        return jdbcTemplate.query(
                "SELECT id, character_id, event_id, trigger_count, last_triggered_at, next_available_at, completed "
                        + "FROM map_event_states WHERE character_id = ? ORDER BY updated_at DESC, id DESC",
                mapper,
                characterId);
    }

    public void markTriggered(long characterId, String eventId, LocalDateTime now,
                              LocalDateTime nextAvailableAt, boolean completed) {
        MapEventStateRecord existing = find(characterId, eventId);
        if (existing == null) {
            jdbcTemplate.update(
                    "INSERT INTO map_event_states(character_id, event_id, trigger_count, last_triggered_at, next_available_at, completed) "
                            + "VALUES(?, ?, 1, ?, ?, ?)",
                    characterId,
                    eventId,
                    format(now),
                    format(nextAvailableAt),
                    completed ? 1 : 0);
        } else {
            jdbcTemplate.update(
                    "UPDATE map_event_states SET trigger_count = trigger_count + 1, last_triggered_at = ?, "
                            + "next_available_at = ?, completed = ?, updated_at = CURRENT_TIMESTAMP "
                            + "WHERE character_id = ? AND event_id = ?",
                    format(now),
                    format(nextAvailableAt),
                    completed ? 1 : 0,
                    characterId,
                    eventId);
        }
    }

    public int delete(long characterId, String eventId) {
        return jdbcTemplate.update(
                "DELETE FROM map_event_states WHERE character_id = ? AND event_id = ?",
                characterId,
                eventId);
    }

    public int deleteByCharacterId(long characterId) {
        return jdbcTemplate.update(
                "DELETE FROM map_event_states WHERE character_id = ?",
                characterId);
    }

    public int cleanupRepeatableBefore(long characterId, LocalDateTime cutoff) {
        String cutoffValue = format(cutoff);
        return jdbcTemplate.update(
                "DELETE FROM map_event_states "
                        + "WHERE character_id = ? AND completed = 0 "
                        + "AND (next_available_at IS NULL OR next_available_at <= ?) "
                        + "AND (last_triggered_at IS NULL OR last_triggered_at <= ?)",
                characterId,
                cutoffValue,
                cutoffValue);
    }

    private static LocalDateTime parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value, FORMATTER);
    }

    private static String format(LocalDateTime value) {
        return value == null ? null : FORMATTER.format(value);
    }
}
