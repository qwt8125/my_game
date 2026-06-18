package com.paly.legend.battleprep;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BattlePreparationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<BattlePreparation> mapper = (rs, rowNum) -> {
        BattlePreparation preparation = new BattlePreparation();
        preparation.setCharacterId(rs.getLong("character_id"));
        preparation.setBonusHp(rs.getInt("bonus_hp"));
        preparation.setBonusAttack(rs.getInt("bonus_attack"));
        preparation.setBonusDefense(rs.getInt("bonus_defense"));
        preparation.setBonusAttackSpeed(rs.getInt("bonus_attack_speed"));
        return preparation;
    };

    public BattlePreparationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BattlePreparation findByCharacterId(long characterId) {
        List<BattlePreparation> values = jdbcTemplate.query(
                "SELECT character_id, bonus_hp, bonus_attack, bonus_defense, bonus_attack_speed FROM battle_preparations WHERE character_id = ?",
                mapper,
                characterId);
        return values.isEmpty() ? null : values.get(0);
    }

    public void addBonus(long characterId, int hp, int attack, int defense, int attackSpeed) {
        jdbcTemplate.update(
                "INSERT INTO battle_preparations(character_id, bonus_hp, bonus_attack, bonus_defense, bonus_attack_speed) "
                        + "VALUES(?, ?, ?, ?, ?) "
                        + "ON CONFLICT(character_id) DO UPDATE SET "
                        + "bonus_hp = bonus_hp + excluded.bonus_hp, "
                        + "bonus_attack = bonus_attack + excluded.bonus_attack, "
                        + "bonus_defense = bonus_defense + excluded.bonus_defense, "
                        + "bonus_attack_speed = bonus_attack_speed + excluded.bonus_attack_speed, "
                        + "updated_at = CURRENT_TIMESTAMP",
                characterId,
                hp,
                attack,
                defense,
                attackSpeed);
    }

    public void deleteByCharacterId(long characterId) {
        jdbcTemplate.update("DELETE FROM battle_preparations WHERE character_id = ?", characterId);
    }
}
