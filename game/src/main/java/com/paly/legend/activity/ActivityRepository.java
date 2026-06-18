package com.paly.legend.activity;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityRepository {

    private final JdbcTemplate jdbcTemplate;

    public ActivityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasClaimed(long characterId, String activityId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM activity_claims WHERE character_id = ? AND activity_id = ?",
                Integer.class,
                characterId,
                activityId);
        return count != null && count > 0;
    }

    public List<String> claimedActivityIds(long characterId) {
        return jdbcTemplate.query(
                "SELECT activity_id FROM activity_claims WHERE character_id = ?",
                (rs, rowNum) -> rs.getString("activity_id"),
                characterId);
    }

    public void markClaimed(long characterId, String activityId, int goldGained, String itemsJson) {
        jdbcTemplate.update(
                "INSERT INTO activity_claims(character_id, activity_id, gold_gained, items_json) VALUES(?, ?, ?, ?)",
                characterId,
                activityId,
                goldGained,
                itemsJson == null ? "[]" : itemsJson);
    }
}
