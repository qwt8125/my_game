package com.paly.legend.skill;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SkillRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<CharacterSkillRecord> mapper = (rs, rowNum) -> {
        CharacterSkillRecord record = new CharacterSkillRecord();
        record.setId(rs.getLong("id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setSkillId(rs.getString("skill_id"));
        record.setLevel(rs.getInt("level"));
        record.setSkillSlot(rs.getInt("skill_slot"));
        return record;
    };

    public SkillRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CharacterSkillRecord> findByCharacterId(long characterId) {
        return jdbcTemplate.query(
                "SELECT id, character_id, skill_id, level, skill_slot FROM character_skills WHERE character_id = ? ORDER BY skill_slot, id",
                mapper,
                characterId);
    }

    public CharacterSkillRecord findByCharacterIdAndSkillId(long characterId, String skillId) {
        List<CharacterSkillRecord> records = jdbcTemplate.query(
                "SELECT id, character_id, skill_id, level, skill_slot FROM character_skills WHERE character_id = ? AND skill_id = ?",
                mapper,
                characterId,
                skillId);
        return records.isEmpty() ? null : records.get(0);
    }

    public void learn(long characterId, String skillId, int skillSlot) {
        jdbcTemplate.update(
                "INSERT INTO character_skills(character_id, skill_id, level, skill_slot) VALUES(?, ?, 1, ?)",
                characterId,
                skillId,
                skillSlot);
    }

    public void updateLevel(long characterId, String skillId, int level) {
        jdbcTemplate.update(
                "UPDATE character_skills SET level = ?, updated_at = CURRENT_TIMESTAMP WHERE character_id = ? AND skill_id = ?",
                level,
                characterId,
                skillId);
    }

    public void updateSkillSlot(long characterId, String skillId, int skillSlot) {
        jdbcTemplate.update(
                "UPDATE character_skills SET skill_slot = ?, updated_at = CURRENT_TIMESTAMP WHERE character_id = ? AND skill_id = ?",
                skillSlot,
                characterId,
                skillId);
    }

    public CharacterSkillRecord findByCharacterIdAndSkillSlot(long characterId, int skillSlot) {
        List<CharacterSkillRecord> records = jdbcTemplate.query(
                "SELECT id, character_id, skill_id, level, skill_slot FROM character_skills WHERE character_id = ? AND skill_slot = ?",
                mapper,
                characterId,
                skillSlot);
        return records.isEmpty() ? null : records.get(0);
    }
}
