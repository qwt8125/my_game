package com.paly.legend.boss;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.paly.legend.battle.BattleRepository;
import com.paly.legend.battle.BattleRequest;
import com.paly.legend.battle.BattleResponse;
import com.paly.legend.battle.BattleSessionResponse;
import com.paly.legend.battle.BattleService;
import com.paly.legend.character.CharacterProgression;
import com.paly.legend.character.CharacterProgressionResult;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.BossConfig;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MonsterConfig;
import com.paly.legend.task.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BossService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final BossRepository bossRepository;
    private final BattleService battleService;
    private final CharacterRepository characterRepository;
    private final CharacterProgression characterProgression;
    private final BattleRepository battleRepository;
    private final GameConfigService gameConfigService;
    private final TaskService taskService;

    public BossService(BossRepository bossRepository,
                       BattleService battleService,
                       CharacterRepository characterRepository,
                       CharacterProgression characterProgression,
                       BattleRepository battleRepository,
                       GameConfigService gameConfigService,
                       TaskService taskService) {
        this.bossRepository = bossRepository;
        this.battleService = battleService;
        this.characterRepository = characterRepository;
        this.characterProgression = characterProgression;
        this.battleRepository = battleRepository;
        this.gameConfigService = gameConfigService;
        this.taskService = taskService;
    }

    public List<BossResponse> list() {
        List<BossResponse> result = new ArrayList<BossResponse>();
        LocalDateTime now = LocalDateTime.now();
        for (BossConfig boss : gameConfigService.listBosses()) {
            bossRepository.ensure(boss.getId(), now);
            result.add(toResponse(boss, bossRepository.findByBossId(boss.getId()), now));
        }
        return result;
    }

    @Transactional
    public BattleSessionResponse start(CurrentUser currentUser, String bossId) {
        return battleService.startBoss(currentUser, bossId);
    }

    @Transactional
    public BossFightResponse fight(CurrentUser currentUser, String bossId) {
        PlayerCharacter before = getCharacter(currentUser);
        BossConfig boss = gameConfigService.getBossRequired(bossId);
        BossStateRecord state = bossRepository.findByBossId(boss.getId());
        LocalDateTime now = LocalDateTime.now();
        if (state == null) {
            bossRepository.ensure(boss.getId(), now);
            state = bossRepository.findByBossId(boss.getId());
        }
        if (state.getAvailableAt().isAfter(now)) {
            throw new BusinessException("BOSS_NOT_AVAILABLE", "BOSS 尚未刷新");
        }

        BattleRequest request = new BattleRequest();
        request.setMapId(boss.getMapId());
        request.setMonsterId(boss.getMonsterId());
        BattleResponse battle = battleService.fight(currentUser, request);
        int bonusExp = 0;
        int bonusGold = 0;
        LocalDateTime nextAvailableAt = state.getAvailableAt();

        if (battle.isWin()) {
            PlayerCharacter afterBattle = characterRepository.findByAccountId(currentUser.getAccountId());
            int multiplier = Math.max(1, boss.getRewardMultiplier());
            bonusExp = Math.max(0, battle.getExpGained() * (multiplier - 1));
            bonusGold = Math.max(0, battle.getGoldGained() * (multiplier - 1));
            CharacterProgressionResult progression = characterProgression.applyBattleReward(afterBattle, bonusExp, bonusGold);
            characterRepository.updateAfterBattle(
                    afterBattle.getId(),
                    progression.getLevel(),
                    progression.getExp(),
                    progression.getGold(),
                    progression.getHp(),
                    progression.getAttack(),
                    progression.getDefense(),
                    progression.getAttackSpeed(),
                    progression.getPower());
            if (bonusGold > 0) {
                battleRepository.createCurrencyLog(
                        afterBattle.getId(),
                        bonusGold,
                        afterBattle.getGold(),
                        progression.getGold(),
                        "boss_bonus",
                        boss.getId());
            }
            taskService.onLevelChanged(afterBattle.getId(), progression.getLevel());
            nextAvailableAt = now.plusMinutes(Math.max(1, boss.getRespawnMinutes()));
            bossRepository.markKilled(boss.getId(), before.getId(), nextAvailableAt);
        }

        BossFightResponse response = new BossFightResponse();
        response.setBattle(battle);
        response.setBonusExp(bonusExp);
        response.setBonusGold(bonusGold);
        response.setNextAvailableAt(FORMATTER.format(nextAvailableAt));
        return response;
    }

    private BossResponse toResponse(BossConfig boss, BossStateRecord state, LocalDateTime now) {
        MapConfig map = gameConfigService.getMapRequired(boss.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(boss.getMonsterId());
        BossResponse response = new BossResponse();
        response.setId(boss.getId());
        response.setName(boss.getName());
        response.setMapId(map.getId());
        response.setMapName(map.getName());
        response.setMonsterId(monster.getId());
        response.setMonsterName(monster.getName());
        response.setAvailable(state == null || !state.getAvailableAt().isAfter(now));
        response.setAvailableAt(FORMATTER.format(state == null ? now : state.getAvailableAt()));
        response.setRespawnMinutes(boss.getRespawnMinutes());
        response.setRewardMultiplier(Math.max(1, boss.getRewardMultiplier()));
        return response;
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }
}
