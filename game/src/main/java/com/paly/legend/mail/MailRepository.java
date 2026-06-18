package com.paly.legend.mail;

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
public class MailRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<MailRecord> mapper = (rs, rowNum) -> {
        MailRecord mail = new MailRecord();
        mail.setId(rs.getLong("id"));
        mail.setCharacterId(rs.getLong("character_id"));
        mail.setTitle(rs.getString("title"));
        mail.setContent(rs.getString("content"));
        mail.setAttachmentGold(rs.getInt("attachment_gold"));
        mail.setAttachmentItemId(rs.getString("attachment_item_id"));
        mail.setAttachmentItemType(rs.getString("attachment_item_type"));
        mail.setAttachmentQuantity(rs.getInt("attachment_quantity"));
        mail.setStatus(rs.getInt("status"));
        mail.setReadAt(rs.getString("read_at"));
        mail.setDeleted(rs.getInt("deleted") == 1);
        mail.setExpiresAt(rs.getString("expires_at"));
        mail.setSourceType(rs.getString("source_type"));
        mail.setSourceId(rs.getString("source_id"));
        mail.setCreatedAt(rs.getString("created_at"));
        mail.setClaimedAt(rs.getString("claimed_at"));
        return mail;
    };

    public MailRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long create(final long characterId, final String title, final String content,
                       final int attachmentGold, final String attachmentItemId,
                       final String attachmentItemType, final int attachmentQuantity,
                       final String sourceType, final String sourceId, final String expiresAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO mails(character_id, title, content, attachment_gold, attachment_item_id, attachment_item_type, attachment_quantity, source_type, source_id, expires_at) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, characterId);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setInt(4, attachmentGold);
            ps.setString(5, attachmentItemId);
            ps.setString(6, attachmentItemType);
            ps.setInt(7, attachmentQuantity);
            ps.setString(8, sourceType);
            ps.setString(9, sourceId);
            ps.setString(10, expiresAt);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<MailRecord> listByCharacterId(long characterId, int limit) {
        return jdbcTemplate.query(
                "SELECT id, character_id, title, content, attachment_gold, attachment_item_id, attachment_item_type, attachment_quantity, status, read_at, deleted, expires_at, source_type, source_id, created_at, claimed_at FROM mails WHERE character_id = ? AND deleted = 0 ORDER BY id DESC LIMIT ?",
                mapper,
                characterId,
                Math.max(1, Math.min(100, limit)));
    }

    public MailRecord findByIdForCharacter(long mailId, long characterId) {
        List<MailRecord> mails = jdbcTemplate.query(
                "SELECT id, character_id, title, content, attachment_gold, attachment_item_id, attachment_item_type, attachment_quantity, status, read_at, deleted, expires_at, source_type, source_id, created_at, claimed_at FROM mails WHERE id = ? AND character_id = ? AND deleted = 0",
                mapper,
                mailId,
                characterId);
        return mails.isEmpty() ? null : mails.get(0);
    }

    public void markClaimed(long mailId) {
        jdbcTemplate.update(
                "UPDATE mails SET status = 1, read_at = COALESCE(read_at, CURRENT_TIMESTAMP), claimed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                mailId);
    }

    public void markRead(long mailId) {
        jdbcTemplate.update(
                "UPDATE mails SET read_at = COALESCE(read_at, CURRENT_TIMESTAMP), updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                mailId);
    }

    public void markDeleted(long mailId) {
        jdbcTemplate.update(
                "UPDATE mails SET deleted = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                mailId);
    }
}
