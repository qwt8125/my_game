package com.paly.legend.character;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CharacterRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PlayerCharacter> mapper = (rs, rowNum) -> {
        PlayerCharacter character = new PlayerCharacter();
        character.setId(rs.getLong("id"));
        character.setAccountId(rs.getLong("account_id"));
        character.setNickname(rs.getString("nickname"));
        character.setClassName(rs.getString("class_name"));
        character.setLevel(rs.getInt("level"));
        character.setExp(rs.getInt("exp"));
        character.setGold(rs.getInt("gold"));
        character.setHp(rs.getInt("hp"));
        character.setAttack(rs.getInt("attack"));
        character.setDefense(rs.getInt("defense"));
        character.setAttackSpeed(rs.getInt("attack_speed"));
        character.setPower(rs.getInt("power"));
        character.setCurrentMapId(rs.getString("current_map_id"));
        character.setCurrentNodeId(rs.getString("current_node_id"));
        character.setLastX(rs.getInt("last_x"));
        character.setLastY(rs.getInt("last_y"));
        return character;
    };

    public CharacterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByAccountId(long accountId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM characters WHERE account_id = ?",
                Integer.class,
                accountId);
        return count != null && count > 0;
    }

    public boolean nicknameExists(String nickname) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM characters WHERE nickname = ?",
                Integer.class,
                nickname);
        return count != null && count > 0;
    }

    public Long create(final long accountId, final String nickname, final String className,
                       final int hp, final int attack, final int defense, final int attackSpeed, final int power) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO characters(account_id, nickname, class_name, hp, attack, defense, attack_speed, power) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, accountId);
            ps.setString(2, nickname);
            ps.setString(3, className);
            ps.setInt(4, hp);
            ps.setInt(5, attack);
            ps.setInt(6, defense);
            ps.setInt(7, attackSpeed);
            ps.setInt(8, power);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public PlayerCharacter findByAccountId(long accountId) {
        List<PlayerCharacter> characters = jdbcTemplate.query(
                selectColumns() + " WHERE account_id = ?",
                mapper,
                accountId);
        return characters.isEmpty() ? null : characters.get(0);
    }

    public PlayerCharacter findById(long characterId) {
        List<PlayerCharacter> characters = jdbcTemplate.query(
                selectColumns() + " WHERE id = ?",
                mapper,
                characterId);
        return characters.isEmpty() ? null : characters.get(0);
    }

    public List<PlayerCharacter> listForAdmin(int limit) {
        return jdbcTemplate.query(
                selectColumns() + " ORDER BY id DESC LIMIT ?",
                mapper,
                Math.max(1, Math.min(100, limit)));
    }

    public List<PlayerCharacter> listAll() {
        return jdbcTemplate.query(
                selectColumns() + " ORDER BY id",
                mapper);
    }

    public void addGold(long characterId, int gold) {
        jdbcTemplate.update(
                "UPDATE characters SET gold = gold + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                gold,
                characterId);
    }

    public void updateAfterBattle(long characterId, int level, int exp, int gold,
                                  int hp, int attack, int defense, int attackSpeed, int power) {
        jdbcTemplate.update(
                "UPDATE characters SET level = ?, exp = ?, gold = ?, hp = ?, attack = ?, defense = ?, attack_speed = ?, power = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                level,
                exp,
                gold,
                hp,
                attack,
                defense,
                attackSpeed,
                power,
                characterId);
    }

    public void updateStats(long characterId, int hp, int attack, int defense, int attackSpeed, int power) {
        jdbcTemplate.update(
                "UPDATE characters SET hp = ?, attack = ?, defense = ?, attack_speed = ?, power = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                hp,
                attack,
                defense,
                attackSpeed,
                power,
                characterId);
    }

    public void updateLocation(long characterId, String mapId, String nodeId, int x, int y) {
        jdbcTemplate.update(
                "UPDATE characters SET current_map_id = ?, current_node_id = ?, last_x = ?, last_y = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                mapId,
                nodeId,
                x,
                y,
                characterId);
    }

    private String selectColumns() {
        return "SELECT id, account_id, nickname, class_name, level, exp, gold, hp, attack, defense, attack_speed, power, "
                + "current_map_id, current_node_id, last_x, last_y FROM characters";
    }
}
