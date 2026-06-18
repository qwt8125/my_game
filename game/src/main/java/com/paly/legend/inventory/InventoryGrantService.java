package com.paly.legend.inventory;

import com.paly.legend.config.ItemConfig;
import com.paly.legend.equipment.EquipmentAffixService;
import org.springframework.stereotype.Service;

@Service
public class InventoryGrantService {

    private final InventoryRepository inventoryRepository;
    private final EquipmentAffixService equipmentAffixService;

    public InventoryGrantService(InventoryRepository inventoryRepository, EquipmentAffixService equipmentAffixService) {
        this.inventoryRepository = inventoryRepository;
        this.equipmentAffixService = equipmentAffixService;
    }

    public long addItem(long characterId, ItemConfig item, int quantity) {
        if (!"equipment".equals(item.getType())) {
            return inventoryRepository.addItem(characterId, item.getId(), item.getType(), quantity);
        }
        int safeQuantity = Math.max(1, quantity);
        long firstId = 0;
        for (int i = 0; i < safeQuantity; i++) {
            long id = inventoryRepository.addItemWithExtraJson(
                    characterId,
                    item.getId(),
                    item.getType(),
                    1,
                    equipmentAffixService.initialExtraJson(item));
            if (firstId == 0) {
                firstId = id;
            }
        }
        return firstId;
    }
}
