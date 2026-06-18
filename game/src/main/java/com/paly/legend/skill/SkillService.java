package com.paly.legend.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.CharacterStatService;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.SkillConfig;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.TaskRewardItemConfig;
import com.paly.legend.equipment.CharacterStats;
import com.paly.legend.inventory.InventoryItemRecord;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.task.TaskRewardItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final CharacterRepository characterRepository;
    private final CharacterStatService characterStatService;
    private final GameConfigService gameConfigService;
    private final BattleRepository battleRepository;
    private final InventoryRepository inventoryRepository;

    public SkillService(SkillRepository skillRepository,
                        CharacterRepository characterRepository,
                        CharacterStatService characterStatService,
                        GameConfigService gameConfigService,
                        BattleRepository battleRepository,
                        InventoryRepository inventoryRepository) {
        this.skillRepository = skillRepository;
        this.characterRepository = characterRepository;
        this.characterStatService = characterStatService;
        this.gameConfigService = gameConfigService;
        this.battleRepository = battleRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<SkillResponse> list(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        Map<String, Integer> levels = skillLevels(character.getId());
        List<SkillResponse> responses = new ArrayList<SkillResponse>();
        for (SkillConfig config : gameConfigService.listSkillsByClassName(character.getClassName())) {
            int level = levels.containsKey(config.getId()) ? levels.get(config.getId()) : 0;
            int upgradeGold = upgradeGold(config, level);
            SkillResponse response = SkillResponse.from(
                    config,
                    level,
                    level == 0 && character.getLevel() >= config.getRequiredLevel() && character.getGold() >= upgradeGold && hasMaterials(character.getId(), config),
                    level > 0 && level < config.getMaxLevel() && character.getLevel() >= config.getRequiredLevel() && character.getGold() >= upgradeGold && hasMaterials(character.getId(), config),
                    upgradeGold);
            CharacterSkillRecord record = skillRepository.findByCharacterIdAndSkillId(character.getId(), config.getId());
            response.setSkillSlot(record == null ? 0 : record.getSkillSlot());
            response.setMaterialCosts(materialCosts(config));
            responses.add(response);
        }
        return responses;
    }

    @Transactional
    public List<SkillResponse> learn(CurrentUser currentUser, SkillActionRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        SkillConfig skill = validateSkillForCharacter(character, request.getSkillId());
        if (skillRepository.findByCharacterIdAndSkillId(character.getId(), skill.getId()) != null) {
            throw new BusinessException("SKILL_ALREADY_LEARNED", "技能已经学会");
        }
        if (character.getLevel() < skill.getRequiredLevel()) {
            throw new BusinessException("SKILL_LEVEL_NOT_ENOUGH", "角色等级不足，无法学习该技能");
        }
        int cost = upgradeGold(skill, 0);
        payGold(character, cost, "skill_learn", skill.getId());
        payMaterials(character.getId(), skill, "skill_learn");
        skillRepository.learn(character.getId(), skill.getId(), defaultSkillSlot(character.getId(), skill));
        refreshStats(character);
        return list(currentUser);
    }

    @Transactional
    public List<SkillResponse> upgrade(CurrentUser currentUser, SkillActionRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        SkillConfig skill = validateSkillForCharacter(character, request.getSkillId());
        CharacterSkillRecord record = skillRepository.findByCharacterIdAndSkillId(character.getId(), skill.getId());
        if (record == null) {
            throw new BusinessException("SKILL_NOT_LEARNED", "请先学习该技能");
        }
        if (record.getLevel() >= skill.getMaxLevel()) {
            throw new BusinessException("SKILL_MAX_LEVEL", "技能已达到当前上限");
        }
        int cost = upgradeGold(skill, record.getLevel());
        payGold(character, cost, "skill_upgrade", skill.getId());
        payMaterials(character.getId(), skill, "skill_upgrade");
        skillRepository.updateLevel(character.getId(), skill.getId(), record.getLevel() + 1);
        refreshStats(character);
        return list(currentUser);
    }

    @Transactional
    public List<SkillResponse> updateSlot(CurrentUser currentUser, SkillSlotRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        SkillConfig skill = validateSkillForCharacter(character, request.getSkillId());
        if (!"active".equals(skill.getType())) {
            throw new BusinessException("SKILL_SLOT_ACTIVE_ONLY", "只有主动技能可以放入技能栏");
        }
        CharacterSkillRecord record = skillRepository.findByCharacterIdAndSkillId(character.getId(), skill.getId());
        if (record == null) {
            throw new BusinessException("SKILL_NOT_LEARNED", "请先学习该技能");
        }
        int slot = Math.max(0, Math.min(4, request.getSkillSlot()));
        if (slot > 0) {
            CharacterSkillRecord occupied = skillRepository.findByCharacterIdAndSkillSlot(character.getId(), slot);
            if (occupied != null && !occupied.getSkillId().equals(skill.getId())) {
                skillRepository.updateSkillSlot(character.getId(), occupied.getSkillId(), 0);
            }
        }
        skillRepository.updateSkillSlot(character.getId(), skill.getId(), slot);
        return list(currentUser);
    }

    public Map<String, Integer> skillLevels(long characterId) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (CharacterSkillRecord record : skillRepository.findByCharacterId(characterId)) {
            result.put(record.getSkillId(), record.getLevel());
        }
        return result;
    }

    private SkillConfig validateSkillForCharacter(PlayerCharacter character, String skillId) {
        SkillConfig skill = gameConfigService.getSkillRequired(skillId);
        if (!character.getClassName().equals(skill.getClassName())) {
            throw new BusinessException("SKILL_CLASS_MISMATCH", "该技能不属于当前职业");
        }
        return skill;
    }

    private int upgradeGold(SkillConfig skill, int currentLevel) {
        return Math.max(0, skill.getUpgradeGoldBase() + Math.max(0, currentLevel) * skill.getUpgradeGoldStep());
    }

    private boolean hasMaterials(long characterId, SkillConfig skill) {
        if (skill.getUpgradeMaterials() == null || skill.getUpgradeMaterials().isEmpty()) {
            return true;
        }
        for (TaskRewardItemConfig cost : skill.getUpgradeMaterials()) {
            ItemConfig item = gameConfigService.getItemRequired(cost.getItemId());
            InventoryItemRecord record = inventoryRepository.findStackableByItemIdForCharacter(characterId, item.getId(), item.getType());
            if (record == null || record.getQuantity() < cost.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private void payMaterials(long characterId, SkillConfig skill, String reason) {
        if (skill.getUpgradeMaterials() == null) {
            return;
        }
        for (TaskRewardItemConfig cost : skill.getUpgradeMaterials()) {
            ItemConfig item = gameConfigService.getItemRequired(cost.getItemId());
            InventoryItemRecord record = inventoryRepository.findStackableByItemIdForCharacter(characterId, item.getId(), item.getType());
            if (record == null || record.getQuantity() < cost.getQuantity()) {
                throw new BusinessException("SKILL_MATERIAL_NOT_ENOUGH", "技能材料不足：" + item.getName());
            }
            if (record.getQuantity() == cost.getQuantity()) {
                inventoryRepository.deleteById(record.getId());
            } else {
                inventoryRepository.decreaseQuantity(record.getId(), cost.getQuantity());
            }
            inventoryRepository.createDropLog(characterId, reason, skill.getId(), item.getId(), -cost.getQuantity());
        }
    }

    private List<TaskRewardItemResponse> materialCosts(SkillConfig skill) {
        List<TaskRewardItemResponse> result = new ArrayList<TaskRewardItemResponse>();
        if (skill.getUpgradeMaterials() == null) {
            return result;
        }
        for (TaskRewardItemConfig cost : skill.getUpgradeMaterials()) {
            ItemConfig item = gameConfigService.getItemRequired(cost.getItemId());
            result.add(new TaskRewardItemResponse(item.getId(), item.getName(), cost.getQuantity()));
        }
        return result;
    }

    private int defaultSkillSlot(long characterId, SkillConfig skill) {
        if (!"active".equals(skill.getType())) {
            return 0;
        }
        for (int slot = 1; slot <= 4; slot++) {
            if (skillRepository.findByCharacterIdAndSkillSlot(characterId, slot) == null) {
                return slot;
            }
        }
        return 0;
    }

    private void payGold(PlayerCharacter character, int cost, String reason, String refId) {
        if (character.getGold() < cost) {
            throw new BusinessException("GOLD_NOT_ENOUGH", "金币不足");
        }
        int afterGold = character.getGold() - cost;
        characterRepository.addGold(character.getId(), -cost);
        battleRepository.createCurrencyLog(character.getId(), -cost, character.getGold(), afterGold, reason, refId);
    }

    private void refreshStats(PlayerCharacter character) {
        PlayerCharacter latest = characterRepository.findById(character.getId());
        CharacterStats stats = characterStatService.recalculate(latest);
        characterRepository.updateStats(latest.getId(), stats.getHp(), stats.getAttack(), stats.getDefense(), stats.getAttackSpeed(), stats.getPower());
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }
}
