package com.paly.legend.guild;

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
public class GuildRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<GuildRecord> guildMapper = (rs, rowNum) -> {
        GuildRecord record = new GuildRecord();
        record.setId(rs.getLong("id"));
        record.setName(rs.getString("name"));
        record.setNotice(rs.getString("notice"));
        record.setLeaderCharacterId(rs.getLong("leader_character_id"));
        record.setLeaderNickname(rs.getString("leader_nickname"));
        record.setMemberCount(rs.getInt("member_count"));
        record.setTotalContribution(rs.getInt("total_contribution"));
        record.setCreatedAt(rs.getString("created_at"));
        return record;
    };

    private final RowMapper<GuildMemberRecord> memberMapper = (rs, rowNum) -> {
        GuildMemberRecord record = new GuildMemberRecord();
        record.setGuildId(rs.getLong("guild_id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setNickname(rs.getString("nickname"));
        record.setLevel(rs.getInt("level"));
        record.setPower(rs.getInt("power"));
        record.setRole(rs.getString("role"));
        record.setContribution(rs.getInt("contribution"));
        record.setJoinedAt(rs.getString("joined_at"));
        return record;
    };

    public GuildRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean nameExists(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM guilds WHERE name = ?",
                Integer.class,
                name);
        return count != null && count > 0;
    }

    public GuildRecord findById(long guildId) {
        List<GuildRecord> records = jdbcTemplate.query(guildSelect() + " WHERE g.id = ?", guildMapper, guildId);
        return records.isEmpty() ? null : records.get(0);
    }

    public GuildRecord findByCharacterId(long characterId) {
        List<GuildRecord> records = jdbcTemplate.query(
                guildSelect() + " JOIN guild_members gm ON gm.guild_id = g.id WHERE gm.character_id = ?",
                guildMapper,
                characterId);
        return records.isEmpty() ? null : records.get(0);
    }

    public GuildMemberRecord findMemberByCharacterId(long characterId) {
        List<GuildMemberRecord> records = jdbcTemplate.query(memberSelect() + " WHERE gm.character_id = ?",
                memberMapper,
                characterId);
        return records.isEmpty() ? null : records.get(0);
    }

    public GuildMemberRecord findMember(long guildId, long characterId) {
        List<GuildMemberRecord> records = jdbcTemplate.query(memberSelect() + " WHERE gm.guild_id = ? AND gm.character_id = ?",
                memberMapper,
                guildId,
                characterId);
        return records.isEmpty() ? null : records.get(0);
    }

    public List<GuildRecord> list(int limit) {
        return jdbcTemplate.query(
                guildSelect() + " ORDER BY g.member_count DESC, g.id ASC LIMIT ?",
                guildMapper,
                Math.max(1, Math.min(100, limit)));
    }

    public List<GuildRecord> listByContribution(int limit) {
        return jdbcTemplate.query(
                guildSelect() + " ORDER BY g.total_contribution DESC, g.member_count DESC, g.id ASC LIMIT ?",
                guildMapper,
                Math.max(1, Math.min(100, limit)));
    }

    public List<GuildMemberRecord> listMembers(long guildId) {
        return jdbcTemplate.query(
                memberSelect() + " WHERE gm.guild_id = ? ORDER BY CASE gm.role WHEN 'leader' THEN 0 ELSE 1 END, c.power DESC, c.id ASC",
                memberMapper,
                guildId);
    }

    public long create(final String name, final String notice, final long leaderCharacterId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO guilds(name, notice, leader_character_id, member_count, total_contribution) VALUES(?, ?, ?, 1, 0)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, notice);
            ps.setLong(3, leaderCharacterId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void addMember(long guildId, long characterId, String role) {
        jdbcTemplate.update(
                "INSERT INTO guild_members(guild_id, character_id, role) VALUES(?, ?, ?)",
                guildId,
                characterId,
                role);
    }

    public void removeMember(long guildId, long characterId) {
        jdbcTemplate.update(
                "DELETE FROM guild_members WHERE guild_id = ? AND character_id = ?",
                guildId,
                characterId);
    }

    public void deleteGuild(long guildId) {
        jdbcTemplate.update("DELETE FROM guild_activity_claims WHERE guild_id = ?", guildId);
        jdbcTemplate.update("DELETE FROM guild_shop_purchases WHERE guild_id = ?", guildId);
        jdbcTemplate.update("DELETE FROM guild_donations WHERE guild_id = ?", guildId);
        jdbcTemplate.update("DELETE FROM guild_logs WHERE guild_id = ?", guildId);
        jdbcTemplate.update("DELETE FROM guild_members WHERE guild_id = ?", guildId);
        jdbcTemplate.update("DELETE FROM guilds WHERE id = ?", guildId);
    }

    public void transferLeader(long guildId, long oldLeaderCharacterId, long newLeaderCharacterId) {
        jdbcTemplate.update(
                "UPDATE guilds SET leader_character_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                newLeaderCharacterId,
                guildId);
        jdbcTemplate.update(
                "UPDATE guild_members SET role = 'member', updated_at = CURRENT_TIMESTAMP WHERE guild_id = ? AND character_id = ?",
                guildId,
                oldLeaderCharacterId);
        jdbcTemplate.update(
                "UPDATE guild_members SET role = 'leader', updated_at = CURRENT_TIMESTAMP WHERE guild_id = ? AND character_id = ?",
                guildId,
                newLeaderCharacterId);
    }

    public int countMembers(long guildId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM guild_members WHERE guild_id = ?",
                Integer.class,
                guildId);
        return count == null ? 0 : count;
    }

    public void refreshMemberCount(long guildId) {
        jdbcTemplate.update(
                "UPDATE guilds SET member_count = (SELECT COUNT(1) FROM guild_members WHERE guild_id = ?), updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                guildId,
                guildId);
    }

    public void log(long guildId, Long characterId, String action, String payloadJson) {
        jdbcTemplate.update(
                "INSERT INTO guild_logs(guild_id, character_id, action, payload_json) VALUES(?, ?, ?, ?)",
                guildId,
                characterId,
                action,
                payloadJson == null ? "{}" : payloadJson);
    }

    public void addContribution(long guildId, long characterId, int contribution) {
        jdbcTemplate.update(
                "UPDATE guild_members SET contribution = contribution + ?, updated_at = CURRENT_TIMESTAMP WHERE guild_id = ? AND character_id = ?",
                contribution,
                guildId,
                characterId);
        jdbcTemplate.update(
                "UPDATE guilds SET total_contribution = total_contribution + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                contribution,
                guildId);
    }

    public void recordDonation(long guildId, long characterId, String donationId, int goldCost, int contributionGained) {
        jdbcTemplate.update(
                "INSERT INTO guild_donations(guild_id, character_id, donation_id, gold_cost, contribution_gained) VALUES(?, ?, ?, ?, ?)",
                guildId,
                characterId,
                donationId,
                goldCost,
                contributionGained);
    }

    public int donationCountToday(long characterId, String donationId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM guild_donations WHERE character_id = ? AND donation_id = ? AND date(created_at, 'localtime') = date('now', 'localtime')",
                Integer.class,
                characterId,
                donationId);
        return count == null ? 0 : count;
    }

    public int purchaseCountToday(long characterId, String shopItemId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM guild_shop_purchases WHERE character_id = ? AND shop_item_id = ? AND date(created_at, 'localtime') = date('now', 'localtime')",
                Integer.class,
                characterId,
                shopItemId);
        return count == null ? 0 : count;
    }

    public void consumeContribution(long guildId, long characterId, int contribution) {
        jdbcTemplate.update(
                "UPDATE guild_members SET contribution = contribution - ?, updated_at = CURRENT_TIMESTAMP WHERE guild_id = ? AND character_id = ?",
                contribution,
                guildId,
                characterId);
    }

    public void recordShopPurchase(long guildId, long characterId, String shopItemId, String itemId,
                                   int quantity, int contributionCost, int goldCost) {
        jdbcTemplate.update(
                "INSERT INTO guild_shop_purchases(guild_id, character_id, shop_item_id, item_id, quantity, contribution_cost, gold_cost) VALUES(?, ?, ?, ?, ?, ?, ?)",
                guildId,
                characterId,
                shopItemId,
                itemId,
                quantity,
                contributionCost,
                goldCost);
    }

    public boolean hasClaimedActivity(long guildId, long characterId, String activityId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM guild_activity_claims WHERE guild_id = ? AND character_id = ? AND activity_id = ?",
                Integer.class,
                guildId,
                characterId,
                activityId);
        return count != null && count > 0;
    }

    public void recordActivityClaim(long guildId, long characterId, String activityId, int rewardGold, String rewardItemsJson) {
        jdbcTemplate.update(
                "INSERT INTO guild_activity_claims(guild_id, character_id, activity_id, reward_gold, reward_items_json) VALUES(?, ?, ?, ?, ?)",
                guildId,
                characterId,
                activityId,
                rewardGold,
                rewardItemsJson == null ? "[]" : rewardItemsJson);
    }

    private String guildSelect() {
        return "SELECT g.id, g.name, g.notice, g.leader_character_id, c.nickname AS leader_nickname, g.member_count, g.total_contribution, g.created_at "
                + "FROM guilds g JOIN characters c ON c.id = g.leader_character_id";
    }

    private String memberSelect() {
        return "SELECT gm.guild_id, gm.character_id, c.nickname, c.level, c.power, gm.role, gm.contribution, gm.joined_at "
                + "FROM guild_members gm JOIN characters c ON c.id = gm.character_id";
    }
}
