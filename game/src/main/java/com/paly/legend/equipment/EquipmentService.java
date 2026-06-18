package com.paly.legend.equipment;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterProgression;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.CharacterStatService;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.EnhancementRuleConfig;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.TaskRewardItemConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryItemRecord;
import com.paly.legend.inventory.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryCapacityService inventoryCapacityService;
    private final CharacterRepository characterRepository;
    private final CharacterProgression characterProgression;
    private final CharacterStatService characterStatService;
    private final GameConfigService gameConfigService;
    private final EquipmentAffixService equipmentAffixService;
    private final BattleRepository battleRepository;
    private final ObjectMapper objectMapper;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            InventoryRepository inventoryRepository,
                            InventoryCapacityService inventoryCapacityService,
                            CharacterRepository characterRepository,
                            CharacterProgression characterProgression,
                            CharacterStatService characterStatService,
                            GameConfigService gameConfigService,
                            EquipmentAffixService equipmentAffixService,
                            BattleRepository battleRepository,
                            ObjectMapper objectMapper) {
        this.equipmentRepository = equipmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryCapacityService = inventoryCapacityService;
        this.characterRepository = characterRepository;
        this.characterProgression = characterProgression;
        this.characterStatService = characterStatService;
        this.gameConfigService = gameConfigService;
        this.equipmentAffixService = equipmentAffixService;
        this.battleRepository = battleRepository;
        this.objectMapper = objectMapper;
    }

    public EquipmentResponse list(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        return buildResponse(character);
    }

    @Transactional
    public EquipmentResponse equip(CurrentUser currentUser, EquipItemRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        InventoryItemRecord inventoryItem = inventoryRepository.findByIdForCharacter(
                request.getInventoryItemId(),
                character.getId());
        if (inventoryItem == null) {
            throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
        }
        if (inventoryItem.getQuantity() != 1) {
            throw new BusinessException("EQUIPMENT_QUANTITY_INVALID", "装备物品数量异常，无法穿戴");
        }

        ItemConfig item = gameConfigService.getItemRequired(inventoryItem.getItemId());
        validateEquipment(character, item);

        EquipmentRecord equippedRecord = equipmentRepository.findByInventoryItemId(inventoryItem.getId());
        if (equippedRecord != null && equippedRecord.getCharacterId() == character.getId()) {
            return buildResponse(character);
        }
        if (equippedRecord != null) {
            throw new BusinessException("EQUIPMENT_ALREADY_USED", "该装备已经被穿戴");
        }

        equipmentRepository.equip(character.getId(), item.getSlot(), inventoryItem.getId());
        CharacterStats stats = recalculateStats(character);
        characterRepository.updateStats(character.getId(), stats.getHp(), stats.getAttack(), stats.getDefense(), stats.getAttackSpeed(), stats.getPower());
        return buildResponse(characterRepository.findByAccountId(currentUser.getAccountId()));
    }

    @Transactional
    public EnhanceItemResponse enhance(CurrentUser currentUser, EnhanceItemRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        InventoryItemRecord inventoryItem = inventoryRepository.findByIdForCharacter(
                request.getInventoryItemId(),
                character.getId());
        if (inventoryItem == null) {
            throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
        }
        ItemConfig item = gameConfigService.getItemRequired(inventoryItem.getItemId());
        if (!"equipment".equals(item.getType())) {
            throw new BusinessException("ITEM_NOT_EQUIPMENT", "该物品不是装备");
        }
        EquipmentEnhancement enhancement = EquipmentEnhancement.read(objectMapper, inventoryItem);
        int currentLevel = Math.max(0, enhancement.getEnhanceLevel());
        int nextLevel = currentLevel + 1;
        EnhancementRuleConfig rule = gameConfigService.getEnhancementRule(item, nextLevel);
        if (rule == null) {
            throw new BusinessException("EQUIPMENT_ENHANCE_MAX", "装备已强化到当前上限");
        }
        int cost = rule.getGoldCost();
        if (character.getGold() < cost) {
            throw new BusinessException("GOLD_NOT_ENOUGH", "金币不足，无法强化");
        }
        List<EnhancementMaterialCost> materialCosts = materialCostsForRule(rule);
        requireEnhanceMaterials(character.getId(), materialCosts);

        enhancement.setEnhanceLevel(nextLevel);
        inventoryRepository.updateExtraJson(inventoryItem.getId(), toJson(enhancement));
        consumeEnhanceMaterials(character.getId(), materialCosts, inventoryItem.getId());
        int afterGold = character.getGold() - cost;
        characterRepository.addGold(character.getId(), -cost);
        battleRepository.createCurrencyLog(
                character.getId(),
                -cost,
                character.getGold(),
                afterGold,
                "equipment_enhance",
                String.valueOf(inventoryItem.getId()));

        CharacterStats stats = recalculateStats(character);
        characterRepository.updateStats(character.getId(), stats.getHp(), stats.getAttack(), stats.getDefense(), stats.getAttackSpeed(), stats.getPower());

        EnhanceItemResponse response = new EnhanceItemResponse();
        response.setInventoryItemId(inventoryItem.getId());
        response.setEnhanceLevel(enhancement.getEnhanceLevel());
        response.setGoldCost(cost);
        response.setMaterialCosts(materialCosts);
        response.setCurrentGold(afterGold);
        response.setHp(stats.getHp());
        response.setAttack(stats.getAttack());
        response.setDefense(stats.getDefense());
        response.setAttackSpeed(stats.getAttackSpeed());
        response.setPower(stats.getPower());
        return response;
    }

    @Transactional
    public RerollAffixResponse rerollAffixes(CurrentUser currentUser, RerollAffixRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        InventoryItemRecord inventoryItem = inventoryRepository.findByIdForCharacter(
                request.getInventoryItemId(),
                character.getId());
        if (inventoryItem == null) {
            throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
        }
        if (inventoryItem.getQuantity() != 1) {
            throw new BusinessException("EQUIPMENT_QUANTITY_INVALID", "装备物品数量异常，无法重铸词条");
        }
        ItemConfig item = gameConfigService.getItemRequired(inventoryItem.getItemId());
        if (!"equipment".equals(item.getType())) {
            throw new BusinessException("ITEM_NOT_EQUIPMENT", "该物品不是装备");
        }

        EnhancementMaterialCost materialCost = equipmentAffixService.rerollCost(item);
        if (materialCost == null) {
            throw new BusinessException("EQUIPMENT_AFFIX_RULE_MISSING", "装备词条规则缺失");
        }
        List<EnhancementMaterialCost> costs = java.util.Collections.singletonList(materialCost);
        requireEnhanceMaterials(character.getId(), costs);

        EquipmentEnhancement current = EquipmentEnhancement.read(objectMapper, inventoryItem);
        EquipmentEnhancement next = equipmentAffixService.reroll(item, current);
        inventoryRepository.updateExtraJson(inventoryItem.getId(), equipmentAffixService.toJson(next));
        consumeEnhanceMaterials(character.getId(), costs, inventoryItem.getId(), "equipment_affix_reroll");

        CharacterStats stats;
        if (equipmentRepository.isEquipped(inventoryItem.getId())) {
            stats = recalculateStats(character);
            characterRepository.updateStats(character.getId(), stats.getHp(), stats.getAttack(), stats.getDefense(), stats.getAttackSpeed(), stats.getPower());
        } else {
            stats = new CharacterStats(character.getHp(), character.getAttack(), character.getDefense(), character.getAttackSpeed(), character.getPower());
        }

        RerollAffixResponse response = new RerollAffixResponse();
        response.setInventoryItemId(inventoryItem.getId());
        response.setMaterialCost(materialCost);
        response.setAffixes(next.getAffixes());
        response.setSkillTriggerBonus(EquipmentEnhancement.skillTriggerBonus(next));
        response.setHp(EquipmentEnhancement.hp(item, next));
        response.setAttack(EquipmentEnhancement.attack(item, next));
        response.setDefense(EquipmentEnhancement.defense(item, next));
        response.setAttackSpeed(EquipmentEnhancement.attackSpeed(item, next));
        response.setPower(stats.getPower());
        return response;
    }

    @Transactional
    public EquipmentResponse unequip(CurrentUser currentUser, UnequipItemRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        String slot = request.getSlot().trim().toLowerCase();
        EquipmentRecord record = equipmentRepository.findBySlot(character.getId(), slot);
        if (record == null) {
            throw new BusinessException("EQUIPMENT_SLOT_EMPTY", "该部位未穿戴装备");
        }
        inventoryCapacityService.requireSpaceFor(character.getId(),
                java.util.Collections.singletonList(new InventoryItemGrant(record.getItemId(), 1)));

        equipmentRepository.unequip(character.getId(), slot);
        CharacterStats stats = recalculateStats(character);
        characterRepository.updateStats(character.getId(), stats.getHp(), stats.getAttack(), stats.getDefense(), stats.getAttackSpeed(), stats.getPower());
        return buildResponse(characterRepository.findByAccountId(currentUser.getAccountId()));
    }

    @Transactional
    public DecomposeItemResponse decompose(CurrentUser currentUser, DecomposeItemRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        InventoryItemRecord inventoryItem = inventoryRepository.findByIdForCharacter(
                request.getInventoryItemId(),
                character.getId());
        if (inventoryItem == null) {
            throw new BusinessException("INVENTORY_ITEM_NOT_FOUND", "背包物品不存在");
        }
        if (equipmentRepository.isEquipped(inventoryItem.getId())) {
            throw new BusinessException("EQUIPMENT_DECOMPOSE_EQUIPPED", "已穿戴装备不能分解");
        }
        if (inventoryItem.getQuantity() != 1) {
            throw new BusinessException("EQUIPMENT_QUANTITY_INVALID", "装备物品数量异常，无法分解");
        }
        ItemConfig item = gameConfigService.getItemRequired(inventoryItem.getItemId());
        if (!"equipment".equals(item.getType())) {
            throw new BusinessException("ITEM_NOT_EQUIPMENT", "该物品不是装备");
        }

        int enhanceLevel = EquipmentEnhancement.read(objectMapper, inventoryItem).getEnhanceLevel();
        List<EnhancementMaterialCost> materials = decomposeMaterials(item, enhanceLevel);
        inventoryRepository.deleteById(inventoryItem.getId());
        for (EnhancementMaterialCost material : materials) {
            inventoryRepository.addItem(character.getId(), material.getItemId(), "material", material.getQuantity());
            inventoryRepository.createDropLog(character.getId(), "equipment_decompose", String.valueOf(inventoryItem.getId()), material.getItemId(), material.getQuantity());
        }
        inventoryRepository.createDropLog(character.getId(), "equipment_decompose", String.valueOf(inventoryItem.getId()), item.getId(), -1);

        DecomposeItemResponse response = new DecomposeItemResponse();
        response.setInventoryItemId(inventoryItem.getId());
        response.setItemId(item.getId());
        response.setName(item.getName());
        response.setEnhanceLevel(enhanceLevel);
        response.setMaterials(materials);
        return response;
    }

    public CharacterStats recalculateStats(PlayerCharacter character) {
        return characterStatService.recalculate(character);
    }

    private EquipmentResponse buildResponse(PlayerCharacter character) {
        EquipmentResponse response = new EquipmentResponse();
        List<EquipmentItemResponse> items = new ArrayList<EquipmentItemResponse>();
        List<ItemConfig> equippedItems = new ArrayList<ItemConfig>();
        for (EquipmentRecord record : equipmentRepository.findByCharacterId(character.getId())) {
            InventoryItemRecord inventoryItem = inventoryRepository.findByIdForCharacter(record.getInventoryItemId(), character.getId());
            EquipmentEnhancement enhancement = EquipmentEnhancement.read(objectMapper, inventoryItem);
            ItemConfig item = gameConfigService.getItemRequired(record.getItemId());
            equippedItems.add(item);
            items.add(EquipmentItemResponse.from(record, item, enhancement));
        }
        response.setItems(items);
        response.setSetBonuses(EquipmentSetBonusCalculator.bonusesFor(equippedItems));
        response.setHp(character.getHp());
        response.setAttack(character.getAttack());
        response.setDefense(character.getDefense());
        response.setAttackSpeed(character.getAttackSpeed());
        response.setPower(character.getPower());
        return response;
    }

    private void validateEquipment(PlayerCharacter character, ItemConfig item) {
        if (!"equipment".equals(item.getType())) {
            throw new BusinessException("ITEM_NOT_EQUIPMENT", "该物品不是装备");
        }
        if (item.getSlot() == null || item.getSlot().trim().isEmpty()) {
            throw new BusinessException("EQUIPMENT_SLOT_INVALID", "装备部位配置异常");
        }
        if (character.getLevel() < item.getRequiredLevel()) {
            throw new BusinessException("EQUIPMENT_LEVEL_NOT_ENOUGH", "角色等级不足，无法穿戴该装备");
        }
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }

    private String toJson(EquipmentEnhancement enhancement) {
        try {
            return objectMapper.writeValueAsString(enhancement);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize enhancement", ex);
        }
    }

    private List<EnhancementMaterialCost> materialCostsForRule(EnhancementRuleConfig rule) {
        List<EnhancementMaterialCost> costs = new ArrayList<EnhancementMaterialCost>();
        if (rule.getMaterialCosts() == null) {
            return costs;
        }
        for (TaskRewardItemConfig item : rule.getMaterialCosts()) {
            costs.add(materialCost(item.getItemId(), item.getQuantity()));
        }
        return costs;
    }

    private EnhancementMaterialCost materialCost(String itemId, int quantity) {
        ItemConfig item = gameConfigService.getItemRequired(itemId);
        return new EnhancementMaterialCost(item.getId(), item.getName(), quantity);
    }

    private void requireEnhanceMaterials(long characterId, List<EnhancementMaterialCost> costs) {
        for (EnhancementMaterialCost cost : costs) {
            InventoryItemRecord material = inventoryRepository.findMaterialByItemIdForCharacter(characterId, cost.getItemId());
            int owned = material == null ? 0 : material.getQuantity();
            if (owned < cost.getQuantity()) {
                throw new BusinessException(
                        "EQUIPMENT_ENHANCE_MATERIAL_NOT_ENOUGH",
                        cost.getName() + "不足，需要 " + cost.getQuantity() + " 个，当前 " + owned + " 个");
            }
        }
    }

    private void consumeEnhanceMaterials(long characterId, List<EnhancementMaterialCost> costs, long inventoryItemId) {
        consumeEnhanceMaterials(characterId, costs, inventoryItemId, "equipment_enhance");
    }

    private void consumeEnhanceMaterials(long characterId, List<EnhancementMaterialCost> costs, long inventoryItemId, String reason) {
        for (EnhancementMaterialCost cost : costs) {
            InventoryItemRecord material = inventoryRepository.findMaterialByItemIdForCharacter(characterId, cost.getItemId());
            if (material.getQuantity() == cost.getQuantity()) {
                inventoryRepository.deleteById(material.getId());
            } else {
                inventoryRepository.decreaseQuantity(material.getId(), cost.getQuantity());
            }
            inventoryRepository.createDropLog(characterId, reason, String.valueOf(inventoryItemId), cost.getItemId(), -cost.getQuantity());
        }
    }

    private List<EnhancementMaterialCost> decomposeMaterials(ItemConfig item, int enhanceLevel) {
        List<EnhancementMaterialCost> result = new ArrayList<EnhancementMaterialCost>();
        String materialId = decomposeMaterialId(item.getQuality());
        int quantity = decomposeBaseQuantity(item.getQuality()) + Math.max(0, enhanceLevel);
        result.add(materialCost(materialId, quantity));
        return result;
    }

    private String decomposeMaterialId(String quality) {
        if ("fine".equals(quality)) {
            return "mat_fine_essence";
        }
        if ("rare".equals(quality)) {
            return "mat_rare_essence";
        }
        if ("epic".equals(quality) || "legendary".equals(quality)) {
            return "mat_epic_essence";
        }
        return "mat_common_essence";
    }

    private int decomposeBaseQuantity(String quality) {
        if ("epic".equals(quality) || "legendary".equals(quality)) {
            return 2;
        }
        return 1;
    }
}
