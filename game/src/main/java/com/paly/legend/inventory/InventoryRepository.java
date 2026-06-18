package com.paly.legend.inventory;

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
public class InventoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<InventoryItemRecord> mapper = (rs, rowNum) -> {
        InventoryItemRecord item = new InventoryItemRecord();
        item.setId(rs.getLong("id"));
        item.setCharacterId(rs.getLong("character_id"));
        item.setItemId(rs.getString("item_id"));
        item.setItemType(rs.getString("item_type"));
        item.setQuantity(rs.getInt("quantity"));
        item.setBindStatus(rs.getInt("bind_status"));
        item.setExtraJson(rs.getString("extra_json"));
        return item;
    };

    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long addItem(final long characterId, final String itemId, final String itemType, final int quantity) {
        int safeQuantity = Math.max(1, quantity);
        if (isStackable(itemType)) {
            InventoryItemRecord existing = findStackableByItemIdForCharacter(characterId, itemId, itemType);
            if (existing != null) {
                jdbcTemplate.update(
                        "UPDATE inventory_items SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        safeQuantity,
                        existing.getId());
                return existing.getId();
            }
            return insertItem(characterId, itemId, itemType, safeQuantity);
        }
        long firstId = 0;
        for (int i = 0; i < safeQuantity; i++) {
            long id = insertItem(characterId, itemId, itemType, 1, "{}");
            if (firstId == 0) {
                firstId = id;
            }
        }
        return firstId;
    }

    public long addItemWithExtraJson(final long characterId, final String itemId, final String itemType,
                                     final int quantity, final String extraJson) {
        if (isStackable(itemType)) {
            return addItem(characterId, itemId, itemType, quantity);
        }
        int safeQuantity = Math.max(1, quantity);
        long firstId = 0;
        for (int i = 0; i < safeQuantity; i++) {
            long id = insertItem(characterId, itemId, itemType, 1, safeExtraJson(extraJson));
            if (firstId == 0) {
                firstId = id;
            }
        }
        return firstId;
    }

    private boolean isStackable(String itemType) {
        return "material".equals(itemType) || "consumable".equals(itemType);
    }

    private long insertItem(final long characterId, final String itemId, final String itemType, final int quantity) {
        return insertItem(characterId, itemId, itemType, quantity, "{}");
    }

    private long insertItem(final long characterId, final String itemId, final String itemType,
                            final int quantity, final String extraJson) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO inventory_items(character_id, item_id, item_type, quantity, extra_json) VALUES(?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, characterId);
            ps.setString(2, itemId);
            ps.setString(3, itemType);
            ps.setInt(4, quantity);
            ps.setString(5, safeExtraJson(extraJson));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private String safeExtraJson(String extraJson) {
        return extraJson == null || extraJson.trim().isEmpty() ? "{}" : extraJson;
    }

    public void createDropLog(long characterId, String sourceType, String sourceId, String itemId, int quantity) {
        jdbcTemplate.update(
                "INSERT INTO drop_logs(character_id, source_type, source_id, item_id, quantity) VALUES(?, ?, ?, ?, ?)",
                characterId,
                sourceType,
                sourceId,
                itemId,
                quantity);
    }

    public List<InventoryItemRecord> findByCharacterId(long characterId) {
        return jdbcTemplate.query(
                "SELECT id, character_id, item_id, item_type, quantity, bind_status, extra_json FROM inventory_items WHERE character_id = ? ORDER BY id DESC",
                mapper,
                characterId);
    }

    public List<InventoryItemRecord> findUnequippedByCharacterId(long characterId) {
        return jdbcTemplate.query(
                "SELECT i.id, i.character_id, i.item_id, i.item_type, i.quantity, i.bind_status, i.extra_json "
                        + "FROM inventory_items i "
                        + "LEFT JOIN equipped_items e ON e.inventory_item_id = i.id "
                        + "WHERE i.character_id = ? AND e.id IS NULL "
                        + "ORDER BY i.id DESC",
                mapper,
                characterId);
    }

    public int countSlots(long characterId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM inventory_items WHERE character_id = ?",
                Integer.class,
                characterId);
        return count == null ? 0 : count;
    }

    public int countUnequippedSlots(long characterId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM inventory_items i "
                        + "LEFT JOIN equipped_items e ON e.inventory_item_id = i.id "
                        + "WHERE i.character_id = ? AND e.id IS NULL",
                Integer.class,
                characterId);
        return count == null ? 0 : count;
    }

    public InventoryItemRecord findByIdForCharacter(long inventoryItemId, long characterId) {
        List<InventoryItemRecord> items = jdbcTemplate.query(
                "SELECT id, character_id, item_id, item_type, quantity, bind_status, extra_json FROM inventory_items WHERE id = ? AND character_id = ?",
                mapper,
                inventoryItemId,
                characterId);
        return items.isEmpty() ? null : items.get(0);
    }

    public InventoryItemRecord findMaterialByItemIdForCharacter(long characterId, String itemId) {
        return findStackableByItemIdForCharacter(characterId, itemId, "material");
    }

    public InventoryItemRecord findStackableByItemIdForCharacter(long characterId, String itemId, String itemType) {
        List<InventoryItemRecord> items = jdbcTemplate.query(
                "SELECT id, character_id, item_id, item_type, quantity, bind_status, extra_json FROM inventory_items WHERE character_id = ? AND item_id = ? AND item_type = ? ORDER BY id LIMIT 1",
                mapper,
                characterId,
                itemId,
                itemType);
        return items.isEmpty() ? null : items.get(0);
    }

    public void decreaseQuantity(long inventoryItemId, int quantity) {
        jdbcTemplate.update(
                "UPDATE inventory_items SET quantity = quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                quantity,
                inventoryItemId);
    }

    public void deleteById(long inventoryItemId) {
        jdbcTemplate.update("DELETE FROM inventory_items WHERE id = ?", inventoryItemId);
    }

    public void updateExtraJson(long inventoryItemId, String extraJson) {
        jdbcTemplate.update(
                "UPDATE inventory_items SET extra_json = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                extraJson,
                inventoryItemId);
    }
}
