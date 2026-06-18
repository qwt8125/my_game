package com.paly.legend.character;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.SkillConfig;
import com.paly.legend.config.TalentConfig;
import com.paly.legend.equipment.CharacterStats;
import com.paly.legend.equipment.EquipmentEnhancement;
import com.paly.legend.equipment.EquipmentRecord;
import com.paly.legend.equipment.EquipmentRepository;
import com.paly.legend.equipment.EquipmentSetBonusCalculator;
import com.paly.legend.inventory.InventoryItemRecord;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.skill.CharacterSkillRecord;
import com.paly.legend.skill.SkillRepository;
import com.paly.legend.talent.CharacterTalentRecord;
import com.paly.legend.talent.TalentRepository;
import org.springframework.stereotype.Service;

@Service
public class CharacterStatService {

    private final CharacterProgression characterProgression;
    private final GameConfigService gameConfigService;
    private final EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final SkillRepository skillRepository;
    private final TalentRepository talentRepository;
    private final ObjectMapper objectMapper;

    public CharacterStatService(CharacterProgression characterProgression,
                                GameConfigService gameConfigService,
                                EquipmentRepository equipmentRepository,
                                InventoryRepository inventoryRepository,
                                SkillRepository skillRepository,
                                TalentRepository talentRepository,
                                ObjectMapper objectMapper) {
        this.characterProgression = characterProgression;
        this.gameConfigService = gameConfigService;
        this.equipmentRepository = equipmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.skillRepository = skillRepository;
        this.talentRepository = talentRepository;
        this.objectMapper = objectMapper;
    }

    public CharacterStats recalculate(PlayerCharacter character) {
        int hp = characterProgression.baseHp(character.getLevel(), character.getClassName());
        int attack = characterProgression.baseAttack(character.getLevel(), character.getClassName());
        int defense = characterProgression.baseDefense(character.getLevel(), character.getClassName());
        int attackSpeed = characterProgression.baseAttackSpeed(character.getLevel(), character.getClassName());

        List<ItemConfig> equippedItems = new java.util.ArrayList<ItemConfig>();
        for (EquipmentRecord record : equipmentRepository.findByCharacterId(character.getId())) {
            ItemConfig item = gameConfigService.getItemRequired(record.getItemId());
            equippedItems.add(item);
            InventoryItemRecord inventoryItem = inventoryRepository.findByIdForCharacter(record.getInventoryItemId(), character.getId());
            EquipmentEnhancement enhancement = EquipmentEnhancement.read(objectMapper, inventoryItem);
            hp += EquipmentEnhancement.hp(item, enhancement);
            attack += EquipmentEnhancement.attack(item, enhancement);
            defense += EquipmentEnhancement.defense(item, enhancement);
            attackSpeed += EquipmentEnhancement.attackSpeed(item, enhancement);
        }
        CharacterStats setStats = EquipmentSetBonusCalculator.activeStats(equippedItems);
        hp += setStats.getHp();
        attack += setStats.getAttack();
        defense += setStats.getDefense();
        attackSpeed += setStats.getAttackSpeed();

        for (CharacterSkillRecord record : skillRepository.findByCharacterId(character.getId())) {
            SkillConfig skill = gameConfigService.getSkillRequired(record.getSkillId());
            if (!"passive".equals(skill.getType())) {
                continue;
            }
            int level = Math.max(1, record.getLevel());
            hp += skill.getPassiveHp() * level;
            attack += skill.getPassiveAttack() * level;
            defense += skill.getPassiveDefense() * level;
            attackSpeed += skill.getPassiveAttackSpeed() * level;
        }

        for (CharacterTalentRecord record : talentRepository.findByCharacterId(character.getId())) {
            TalentConfig talent = gameConfigService.getTalentRequired(record.getTalentId());
            int level = Math.max(1, record.getLevel());
            hp += talent.getHp() * level;
            attack += talent.getAttack() * level;
            defense += talent.getDefense() * level;
            attackSpeed += talent.getAttackSpeed() * level;
        }

        int power = characterProgression.calculatePower(hp, attack, defense, attackSpeed);
        return new CharacterStats(hp, attack, defense, attackSpeed, power);
    }

    public int totalTalentPoints(PlayerCharacter character) {
        return Math.max(0, character.getLevel() - 1);
    }

    public int usedTalentPoints(long characterId) {
        int used = 0;
        for (CharacterTalentRecord record : talentRepository.findByCharacterId(characterId)) {
            used += Math.max(0, record.getLevel());
        }
        return used;
    }

    public double skillTriggerBonus(long characterId) {
        double bonus = 0;
        for (CharacterTalentRecord record : talentRepository.findByCharacterId(characterId)) {
            TalentConfig talent = gameConfigService.getTalentRequired(record.getTalentId());
            bonus += talent.getSkillTriggerBonus() * Math.max(0, record.getLevel());
        }
        for (EquipmentRecord record : equipmentRepository.findByCharacterId(characterId)) {
            InventoryItemRecord inventoryItem = inventoryRepository.findByIdForCharacter(record.getInventoryItemId(), characterId);
            bonus += EquipmentEnhancement.skillTriggerBonus(EquipmentEnhancement.read(objectMapper, inventoryItem));
        }
        return bonus;
    }

    public int goldBonusPercent(long characterId) {
        int bonus = 0;
        for (CharacterTalentRecord record : talentRepository.findByCharacterId(characterId)) {
            TalentConfig talent = gameConfigService.getTalentRequired(record.getTalentId());
            bonus += talent.getGoldBonusPercent() * Math.max(0, record.getLevel());
        }
        return bonus;
    }

    public Map<String, Integer> talentLevelMap(long characterId) {
        List<CharacterTalentRecord> records = talentRepository.findByCharacterId(characterId);
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (CharacterTalentRecord record : records) {
            result.put(record.getTalentId(), record.getLevel());
        }
        return result;
    }
}
