package com.paly.legend.ranking;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RankingRepository {

    private final JdbcTemplate jdbcTemplate;

    public RankingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RankingEntryResponse> list(String orderColumn, int limit) {
        String sql = "SELECT id, nickname, level, power, gold FROM characters ORDER BY " + orderColumn + " DESC, id ASC LIMIT ?";
        List<RankingEntryResponse> entries = jdbcTemplate.query(sql, (rs, rowNum) -> {
            RankingEntryResponse entry = new RankingEntryResponse();
            entry.setRank(rowNum + 1);
            entry.setCharacterId(rs.getLong("id"));
            entry.setNickname(rs.getString("nickname"));
            entry.setLevel(rs.getInt("level"));
            entry.setPower(rs.getInt("power"));
            entry.setGold(rs.getInt("gold"));
            entry.setValue(rs.getInt(orderColumn));
            return entry;
        }, limit);
        return entries;
    }

    public int rankOf(String orderColumn, long characterId) {
        String sql = "SELECT COUNT(1) + 1 FROM characters current "
                + "JOIN characters other ON (other." + orderColumn + " > current." + orderColumn
                + " OR (other." + orderColumn + " = current." + orderColumn + " AND other.id < current.id)) "
                + "WHERE current.id = ?";
        Integer rank = jdbcTemplate.queryForObject(sql, Integer.class, characterId);
        return rank == null ? 0 : rank;
    }
}
