package com.paly.legend.inventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.paly.legend.common.BusinessException;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import org.springframework.stereotype.Service;

@Service
public class InventoryCapacityService {

    public static final int DEFAULT_CAPACITY = 40;

    private final InventoryRepository inventoryRepository;
    private final GameConfigService gameConfigService;

    public InventoryCapacityService(InventoryRepository inventoryRepository,
                                    GameConfigService gameConfigService) {
        this.inventoryRepository = inventoryRepository;
        this.gameConfigService = gameConfigService;
    }

    public int capacity(long characterId) {
        return DEFAULT_CAPACITY;
    }

    public int usedSlots(long characterId) {
        return inventoryRepository.countUnequippedSlots(characterId);
    }

    public int remainingSlots(long characterId) {
        return Math.max(0, capacity(characterId) - usedSlots(characterId));
    }

    public boolean hasSpaceFor(long characterId, List<InventoryItemGrant> grants) {
        return requiredNewSlots(characterId, grants) <= remainingSlots(characterId);
    }

    public void requireSpaceFor(long characterId, List<InventoryItemGrant> grants) {
        int required = requiredNewSlots(characterId, grants);
        int remaining = remainingSlots(characterId);
        if (required > remaining) {
            throw new BusinessException(
                    "INVENTORY_FULL",
                    "背包空间不足，需要 " + required + " 个空格，当前剩余 " + remaining + " 格");
        }
    }

    public int requiredNewSlots(long characterId, List<InventoryItemGrant> grants) {
        if (grants == null || grants.isEmpty()) {
            return 0;
        }
        Set<String> existingMaterials = new HashSet<String>();
        for (InventoryItemRecord record : inventoryRepository.findUnequippedByCharacterId(characterId)) {
            if (isStackable(record.getItemType())) {
                existingMaterials.add(record.getItemId());
            }
        }
        Set<String> newMaterialStacks = new HashSet<String>();
        int slots = 0;
        for (InventoryItemGrant grant : grants) {
            if (grant == null || grant.getItemId() == null || grant.getQuantity() <= 0) {
                continue;
            }
            ItemConfig item = gameConfigService.getItemRequired(grant.getItemId());
            if (isStackable(item.getType())) {
                if (!existingMaterials.contains(item.getId()) && !newMaterialStacks.contains(item.getId())) {
                    newMaterialStacks.add(item.getId());
                    slots++;
                }
            } else {
                slots += Math.max(1, grant.getQuantity());
            }
        }
        return slots;
    }

    private boolean isStackable(String itemType) {
        return "material".equals(itemType) || "consumable".equals(itemType);
    }
}
