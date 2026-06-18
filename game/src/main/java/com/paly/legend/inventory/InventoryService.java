package com.paly.legend.inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.paly.legend.battle.BattleRepository;
import com.paly.legend.battleprep.BattlePreparationRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.equipment.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final CharacterRepository characterRepository;
    private final GameConfigService gameConfigService;
    private final BattleRepository battleRepository;
    private final EquipmentRepository equipmentRepository;
    private final InventoryCapacityService inventoryCapacityService;
    private final BattlePreparationRepository battlePreparationRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            CharacterRepository characterRepository,
                            GameConfigService gameConfigService,
                            BattleRepository battleRepository,
                            EquipmentRepository equipmentRepository,
                            InventoryCapacityService inventoryCapacityService,
                            BattlePreparationRepository battlePreparationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.characterRepository = characterRepository;
        this.gameConfigService = gameConfigService;
        this.battleRepository = battleRepository;
        this.equipmentRepository = equipmentRepository;
        this.inventoryCapacityService = inventoryCapacityService;
        this.battlePreparationRepository = battlePreparationRepository;
    }

    @Transactional
    public UseItemResponse use(CurrentUser currentUser, long inventoryItemId) {
        PlayerCharacter character = getCharacter(currentUser);
        InventoryItemRecord item = inventoryRepository.findByIdForCharacter(inventoryItemId, character.getId());
        if (item == null) {
            throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
        }
        if (equipmentRepository.isEquipped(item.getId())) {
            throw new BusinessException("INVENTORY_ITEM_EQUIPPED", "已穿戴装备不能使用");
        }
        ItemConfig config = gameConfigService.getItemRequired(item.getItemId());
        if (!"consumable".equals(config.getType())) {
            throw new BusinessException("INVENTORY_ITEM_NOT_CONSUMABLE", "该物品无法使用");
        }
        if (!hasConsumableEffect(config)) {
            throw new BusinessException("INVENTORY_ITEM_NO_EFFECT", "该消耗品没有可生效的战斗准备效果");
        }

        if (item.getQuantity() <= 1) {
            inventoryRepository.deleteById(item.getId());
        } else {
            inventoryRepository.decreaseQuantity(item.getId(), 1);
        }
        battlePreparationRepository.addBonus(
                character.getId(),
                Math.max(0, config.getBuffHp()),
                Math.max(0, config.getBuffAttack()),
                Math.max(0, config.getBuffDefense()),
                Math.max(0, config.getBuffAttackSpeed()));
        inventoryRepository.createDropLog(character.getId(), "consume_item", String.valueOf(item.getId()), item.getItemId(), -1);

        UseItemResponse response = new UseItemResponse();
        response.setInventoryItemId(item.getId());
        response.setItemId(item.getItemId());
        response.setName(config.getName());
        response.setRemainingQuantity(Math.max(0, item.getQuantity() - 1));
        response.setBonusHp(Math.max(0, config.getBuffHp()));
        response.setBonusAttack(Math.max(0, config.getBuffAttack()));
        response.setBonusDefense(Math.max(0, config.getBuffDefense()));
        response.setBonusAttackSpeed(Math.max(0, config.getBuffAttackSpeed()));
        return response;
    }

    public InventoryListResponse list(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        List<InventoryItemResponse> items = new ArrayList<InventoryItemResponse>();
        for (InventoryItemRecord record : inventoryRepository.findUnequippedByCharacterId(character.getId())) {
            ItemConfig config = gameConfigService.getItemRequired(record.getItemId());
            items.add(InventoryItemResponse.from(record, config));
        }
        InventoryListResponse response = new InventoryListResponse();
        response.setCapacity(inventoryCapacityService.capacity(character.getId()));
        response.setUsedSlots(inventoryCapacityService.usedSlots(character.getId()));
        response.setRemainingSlots(inventoryCapacityService.remainingSlots(character.getId()));
        response.setItems(items);
        return response;
    }

    @Transactional
    public SellItemResponse sell(CurrentUser currentUser, long inventoryItemId, SellItemRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        InventoryItemRecord item = inventoryRepository.findByIdForCharacter(inventoryItemId, character.getId());
        if (item == null) {
            throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
        }
        if (equipmentRepository.isEquipped(item.getId())) {
            throw new BusinessException("INVENTORY_ITEM_EQUIPPED", "已穿戴装备不能出售");
        }

        int quantity = request.getQuantity();
        if (quantity <= 0) {
            throw new BusinessException("INVENTORY_SELL_QUANTITY_INVALID", "出售数量必须大于 0");
        }
        if (quantity > item.getQuantity()) {
            throw new BusinessException("INVENTORY_SELL_QUANTITY_NOT_ENOUGH", "出售数量超过拥有数量");
        }

        ItemConfig config = gameConfigService.getItemRequired(item.getItemId());
        int goldGained = config.getSellGold() * quantity;
        int beforeGold = character.getGold();
        int afterGold = beforeGold + goldGained;

        if (quantity == item.getQuantity()) {
            inventoryRepository.deleteById(item.getId());
        } else {
            inventoryRepository.decreaseQuantity(item.getId(), quantity);
        }

        if (goldGained > 0) {
            characterRepository.addGold(character.getId(), goldGained);
            battleRepository.createCurrencyLog(
                    character.getId(),
                    goldGained,
                    beforeGold,
                    afterGold,
                    "sell_item",
                    String.valueOf(item.getId()));
        }

        return new SellItemResponse(goldGained, afterGold, item.getQuantity() - quantity);
    }

    @Transactional
    public DiscardItemResponse discard(CurrentUser currentUser, long inventoryItemId, DiscardItemRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        InventoryItemRecord item = inventoryRepository.findByIdForCharacter(inventoryItemId, character.getId());
        if (item == null) {
            throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
        }
        if (equipmentRepository.isEquipped(item.getId())) {
            throw new BusinessException("INVENTORY_ITEM_EQUIPPED", "已穿戴装备不能丢弃");
        }
        int quantity = request.getQuantity();
        if (quantity <= 0) {
            throw new BusinessException("INVENTORY_DISCARD_QUANTITY_INVALID", "丢弃数量必须大于 0");
        }
        if (quantity > item.getQuantity()) {
            throw new BusinessException("INVENTORY_DISCARD_QUANTITY_NOT_ENOUGH", "丢弃数量超过拥有数量");
        }

        ItemConfig config = gameConfigService.getItemRequired(item.getItemId());
        if (quantity == item.getQuantity()) {
            inventoryRepository.deleteById(item.getId());
        } else {
            inventoryRepository.decreaseQuantity(item.getId(), quantity);
        }
        inventoryRepository.createDropLog(character.getId(), "discard", String.valueOf(item.getId()), item.getItemId(), -quantity);

        DiscardItemResponse response = new DiscardItemResponse();
        response.setItemId(item.getItemId());
        response.setName(config.getName());
        response.setDiscardedQuantity(quantity);
        response.setRemainingQuantity(item.getQuantity() - quantity);
        return response;
    }

    @Transactional
    public SellItemResponse sellMaterials(CurrentUser currentUser, SellMaterialsRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        Map<Long, Integer> quantityByItemId = aggregateMaterialSellRequest(request);
        int beforeGold = character.getGold();
        int currentGold = beforeGold;
        int totalGold = 0;

        for (Map.Entry<Long, Integer> entry : quantityByItemId.entrySet()) {
            InventoryItemRecord item = inventoryRepository.findByIdForCharacter(entry.getKey(), character.getId());
            if (item == null) {
                throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
            }
            ItemConfig config = gameConfigService.getItemRequired(item.getItemId());
            if (!"material".equals(config.getType())) {
                throw new BusinessException("INVENTORY_BATCH_SELL_ONLY_MATERIAL", "批量出售只支持材料");
            }
            int quantity = entry.getValue();
            if (quantity > item.getQuantity()) {
                throw new BusinessException("INVENTORY_SELL_QUANTITY_NOT_ENOUGH", "出售数量超过拥有数量");
            }
            int goldGained = config.getSellGold() * quantity;
            if (quantity == item.getQuantity()) {
                inventoryRepository.deleteById(item.getId());
            } else {
                inventoryRepository.decreaseQuantity(item.getId(), quantity);
            }
            totalGold += goldGained;
            currentGold += goldGained;
            if (goldGained > 0) {
                battleRepository.createCurrencyLog(
                        character.getId(),
                        goldGained,
                        currentGold - goldGained,
                        currentGold,
                        "sell_material",
                        String.valueOf(item.getId()));
            }
        }

        if (totalGold > 0) {
            characterRepository.addGold(character.getId(), totalGold);
        }
        return new SellItemResponse(totalGold, currentGold, 0);
    }

    private Map<Long, Integer> aggregateMaterialSellRequest(SellMaterialsRequest request) {
        Map<Long, Integer> result = new LinkedHashMap<Long, Integer>();
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("INVENTORY_BATCH_SELL_EMPTY", "请选择要出售的材料");
        }
        for (SellMaterialsRequestItem item : request.getItems()) {
            if (item.getInventoryItemId() <= 0 || item.getQuantity() <= 0) {
                throw new BusinessException("INVENTORY_SELL_QUANTITY_INVALID", "出售数量必须大于 0");
            }
            Integer current = result.get(item.getInventoryItemId());
            result.put(item.getInventoryItemId(), (current == null ? 0 : current) + item.getQuantity());
        }
        return result;
    }

    private boolean hasConsumableEffect(ItemConfig config) {
        return config.getBuffHp() > 0
                || config.getBuffAttack() > 0
                || config.getBuffDefense() > 0
                || config.getBuffAttackSpeed() > 0;
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }
}
