package com.paly.legend.battle;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class BattleRepository {

    private final JdbcTemplate jdbcTemplate;

    public BattleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long createBattleLog(final long characterId, final String mapId, final String monsterId,
                                final boolean win, final int rounds, final int expGained,
                                final int goldGained, final String actionsJson, final String dropsJson) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO battle_logs(character_id, map_id, monster_id, win, rounds, exp_gained, gold_gained, actions_json, drops_json) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, characterId);
            ps.setString(2, mapId);
            ps.setString(3, monsterId);
            ps.setInt(4, win ? 1 : 0);
            ps.setInt(5, rounds);
            ps.setInt(6, expGained);
            ps.setInt(7, goldGained);
            ps.setString(8, actionsJson);
            ps.setString(9, dropsJson);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void createCurrencyLog(long characterId, int changeAmount, int beforeAmount, int afterAmount, String reason, String refId) {
        jdbcTemplate.update(
                "INSERT INTO currency_logs(character_id, currency_type, change_amount, before_amount, after_amount, reason, ref_id) VALUES(?, ?, ?, ?, ?, ?, ?)",
                characterId,
                "gold",
                changeAmount,
                beforeAmount,
                afterAmount,
                reason,
                refId);
    }
}

