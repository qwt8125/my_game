package com.paly.legend.talent;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TalentRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<CharacterTalentRecord> mapper = (rs, rowNum) -> {
        CharacterTalentRecord record = new CharacterTalentRecord();
        record.setId(rs.getLong("id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setTalentId(rs.getString("talent_id"));
        record.setLevel(rs.getInt("level"));
        return record;
    };

    public TalentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CharacterTalentRecord> findByCharacterId(long characterId) {
        return jdbcTemplate.query(
                "SELECT id, character_id, talent_id, level FROM character_talents WHERE character_id = ? ORDER BY id",
                mapper,
                characterId);
    }

    public CharacterTalentRecord findByCharacterIdAndTalentId(long characterId, String talentId) {
        List<CharacterTalentRecord> records = jdbcTemplate.query(
                "SELECT id, character_id, talent_id, level FROM character_talents WHERE character_id = ? AND talent_id = ?",
                mapper,
                characterId,
                talentId);
        return records.isEmpty() ? null : records.get(0);
    }

    public void addOrUpdate(long characterId, String talentId, int level) {
        CharacterTalentRecord existing = findByCharacterIdAndTalentId(characterId, talentId);
        if (existing == null) {
            jdbcTemplate.update(
                    "INSERT INTO character_talents(character_id, talent_id, level) VALUES(?, ?, ?)",
                    characterId,
                    talentId,
                    level);
        } else {
            jdbcTemplate.update(
                    "UPDATE character_talents SET level = ?, updated_at = CURRENT_TIMESTAMP WHERE character_id = ? AND talent_id = ?",
                    level,
                    characterId,
                    talentId);
        }
    }

    public void deleteByCharacterId(long characterId) {
        jdbcTemplate.update("DELETE FROM character_talents WHERE character_id = ?", characterId);
    }
}
