package com.paly.legend.talent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.CharacterStatService;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.TalentConfig;
import com.paly.legend.equipment.CharacterStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TalentService {

    private static final int RESET_GOLD_COST = 100;

    private final TalentRepository talentRepository;
    private final CharacterRepository characterRepository;
    private final CharacterStatService characterStatService;
    private final GameConfigService gameConfigService;
    private final BattleRepository battleRepository;

    public TalentService(TalentRepository talentRepository,
                         CharacterRepository characterRepository,
                         CharacterStatService characterStatService,
                         GameConfigService gameConfigService,
                         BattleRepository battleRepository) {
        this.talentRepository = talentRepository;
        this.characterRepository = characterRepository;
        this.characterStatService = characterStatService;
        this.gameConfigService = gameConfigService;
        this.battleRepository = battleRepository;
    }

    public TalentListResponse list(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        return buildResponse(character);
    }

    @Transactional
    public TalentListResponse upgrade(CurrentUser currentUser, TalentActionRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        TalentConfig talent = gameConfigService.getTalentRequired(request.getTalentId());
        Map<String, Integer> levels = characterStatService.talentLevelMap(character.getId());
        int currentLevel = levels.containsKey(talent.getId()) ? levels.get(talent.getId()) : 0;
        if (character.getLevel() < talent.getRequiredLevel()) {
            throw new BusinessException("TALENT_LEVEL_NOT_ENOUGH", "角色等级不足，无法点亮该天赋");
        }
        if (currentLevel >= talent.getMaxLevel()) {
            throw new BusinessException("TALENT_MAX_LEVEL", "天赋已达到上限");
        }
        int total = characterStatService.totalTalentPoints(character);
        int used = characterStatService.usedTalentPoints(character.getId());
        if (used >= total) {
            throw new BusinessException("TALENT_POINT_NOT_ENOUGH", "可用天赋点不足");
        }
        if (talent.getPreTalentId() != null && !talent.getPreTalentId().trim().isEmpty()) {
            int preLevel = levels.containsKey(talent.getPreTalentId()) ? levels.get(talent.getPreTalentId()) : 0;
            if (preLevel < talent.getPreTalentLevel()) {
                throw new BusinessException("TALENT_PRE_NOT_ENOUGH", "前置天赋等级不足");
            }
        }
        talentRepository.addOrUpdate(character.getId(), talent.getId(), currentLevel + 1);
        refreshStats(character);
        return buildResponse(characterRepository.findById(character.getId()));
    }

    @Transactional
    public TalentListResponse reset(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        int used = characterStatService.usedTalentPoints(character.getId());
        if (used <= 0) {
            return buildResponse(character);
        }
        if (character.getGold() < RESET_GOLD_COST) {
            throw new BusinessException("GOLD_NOT_ENOUGH", "金币不足，无法重置天赋");
        }
        int afterGold = character.getGold() - RESET_GOLD_COST;
        characterRepository.addGold(character.getId(), -RESET_GOLD_COST);
        battleRepository.createCurrencyLog(character.getId(), -RESET_GOLD_COST, character.getGold(), afterGold, "talent_reset", "talents");
        talentRepository.deleteByCharacterId(character.getId());
        refreshStats(character);
        return buildResponse(characterRepository.findById(character.getId()));
    }

    private TalentListResponse buildResponse(PlayerCharacter character) {
        int total = characterStatService.totalTalentPoints(character);
        int used = characterStatService.usedTalentPoints(character.getId());
        Map<String, Integer> levels = characterStatService.talentLevelMap(character.getId());
        List<TalentResponse> talents = new ArrayList<TalentResponse>();
        for (TalentConfig config : gameConfigService.listTalents()) {
            int level = levels.containsKey(config.getId()) ? levels.get(config.getId()) : 0;
            boolean preOk = true;
            if (config.getPreTalentId() != null && !config.getPreTalentId().trim().isEmpty()) {
                int preLevel = levels.containsKey(config.getPreTalentId()) ? levels.get(config.getPreTalentId()) : 0;
                preOk = preLevel >= config.getPreTalentLevel();
            }
            boolean canUpgrade = used < total && level < config.getMaxLevel()
                    && character.getLevel() >= config.getRequiredLevel() && preOk;
            talents.add(TalentResponse.from(config, level, canUpgrade));
        }
        TalentListResponse response = new TalentListResponse();
        response.setTotalPoints(total);
        response.setUsedPoints(used);
        response.setAvailablePoints(Math.max(0, total - used));
        response.setResetGoldCost(RESET_GOLD_COST);
        response.setTalents(talents);
        return response;
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
