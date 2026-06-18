package com.paly.legend.equipment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EquipmentRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<EquipmentRecord> mapper = (rs, rowNum) -> {
        EquipmentRecord record = new EquipmentRecord();
        record.setId(rs.getLong("id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setSlot(rs.getString("slot"));
        record.setInventoryItemId(rs.getLong("inventory_item_id"));
        record.setItemId(rs.getString("item_id"));
        return record;
    };

    public EquipmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EquipmentRecord> findByCharacterId(long characterId) {
        return jdbcTemplate.query(
                "SELECT e.id, e.character_id, e.slot, e.inventory_item_id, i.item_id FROM equipped_items e JOIN inventory_items i ON e.inventory_item_id = i.id WHERE e.character_id = ? ORDER BY e.slot",
                mapper,
                characterId);
    }

    public EquipmentRecord findBySlot(long characterId, String slot) {
        List<EquipmentRecord> records = jdbcTemplate.query(
                "SELECT e.id, e.character_id, e.slot, e.inventory_item_id, i.item_id FROM equipped_items e JOIN inventory_items i ON e.inventory_item_id = i.id WHERE e.character_id = ? AND e.slot = ?",
                mapper,
                characterId,
                slot);
        return records.isEmpty() ? null : records.get(0);
    }

    public EquipmentRecord findByInventoryItemId(long inventoryItemId) {
        List<EquipmentRecord> records = jdbcTemplate.query(
                "SELECT e.id, e.character_id, e.slot, e.inventory_item_id, i.item_id FROM equipped_items e JOIN inventory_items i ON e.inventory_item_id = i.id WHERE e.inventory_item_id = ?",
                mapper,
                inventoryItemId);
        return records.isEmpty() ? null : records.get(0);
    }

    public Set<Long> findEquippedInventoryItemIds(long characterId) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT inventory_item_id FROM equipped_items WHERE character_id = ?",
                (rs, rowNum) -> rs.getLong("inventory_item_id"),
                characterId);
        return new HashSet<Long>(ids);
    }

    public boolean isEquipped(long inventoryItemId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM equipped_items WHERE inventory_item_id = ?",
                Integer.class,
                inventoryItemId);
        return count != null && count > 0;
    }

    public void equip(long characterId, String slot, long inventoryItemId) {
        jdbcTemplate.update("DELETE FROM equipped_items WHERE character_id = ? AND slot = ?", characterId, slot);
        jdbcTemplate.update(
                "INSERT INTO equipped_items(character_id, slot, inventory_item_id) VALUES(?, ?, ?)",
                characterId,
                slot,
                inventoryItemId);
    }

    public void unequip(long characterId, String slot) {
        jdbcTemplate.update("DELETE FROM equipped_items WHERE character_id = ? AND slot = ?", characterId, slot);
    }
}

