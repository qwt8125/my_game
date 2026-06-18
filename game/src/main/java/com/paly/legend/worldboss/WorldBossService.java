package com.paly.legend.worldboss;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.paly.legend.activity.ActivityEffectService;
import com.paly.legend.battle.BattleActionResponse;
import com.paly.legend.battle.BattleSessionResponse;
import com.paly.legend.battle.BattleService;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MonsterConfig;
import com.paly.legend.config.WorldBossConfig;
import com.paly.legend.mail.MailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorldBossService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int RANK_LIMIT = 10;

    private final WorldBossRepository worldBossRepository;
    private final GameConfigService gameConfigService;
    private final CharacterRepository characterRepository;
    private final BattleService battleService;
    private final MailService mailService;
    private final ActivityEffectService activityEffectService;

    public WorldBossService(WorldBossRepository worldBossRepository,
                            GameConfigService gameConfigService,
                            CharacterRepository characterRepository,
                            BattleService battleService,
                            MailService mailService,
                            ActivityEffectService activityEffectService) {
        this.worldBossRepository = worldBossRepository;
        this.gameConfigService = gameConfigService;
        this.characterRepository = characterRepository;
        this.battleService = battleService;
        this.mailService = mailService;
        this.activityEffectService = activityEffectService;
    }

    public List<WorldBossResponse> list() {
        List<WorldBossResponse> result = new ArrayList<WorldBossResponse>();
        LocalDateTime now = LocalDateTime.now();
        for (WorldBossConfig boss : gameConfigService.listWorldBosses()) {
            WorldBossStateRecord state = worldBossRepository.ensure(boss.getId(), maxHp(boss), now);
            result.add(toResponse(boss, state, now, RANK_LIMIT));
        }
        return result;
    }

    public List<WorldBossDamageRankResponse> rankings(String bossId) {
        WorldBossConfig boss = gameConfigService.getWorldBossRequired(bossId);
        worldBossRepository.ensure(boss.getId(), maxHp(boss), LocalDateTime.now());
        return worldBossRepository.listRanks(boss.getId(), RANK_LIMIT);
    }

    @Transactional
    public BattleSessionResponse start(CurrentUser currentUser, String bossId) {
        PlayerCharacter character = requireCharacter(currentUser);
        WorldBossConfig boss = gameConfigService.getWorldBossRequired(bossId);
        validateRequirements(character, boss);
        WorldBossStateRecord state = worldBossRepository.ensure(boss.getId(), maxHp(boss), LocalDateTime.now());
        if (!isAvailable(state, LocalDateTime.now())) {
            throw new BusinessException("WORLD_BOSS_NOT_AVAILABLE", "世界 BOSS 尚未刷新");
        }
        return battleService.startWorldBoss(currentUser, boss.getId());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void onBattleFinished(long characterId, String bossId, long battleId,
                                 List<BattleActionResponse> actions, boolean win) {
        WorldBossConfig boss = gameConfigService.getWorldBossRequired(bossId);
        LocalDateTime now = LocalDateTime.now();
        WorldBossStateRecord state = worldBossRepository.ensure(boss.getId(), maxHp(boss), now);
        if (!isAvailable(state, now)) {
            return;
        }

        int totalDamage = calculatePlayerDamage(actions);
        if (totalDamage <= 0) {
            return;
        }
        worldBossRepository.recordDamage(boss.getId(), characterId, totalDamage, battleId);
        LocalDateTime nextAvailableAt = now.plusMinutes(Math.max(1, boss.getRespawnMinutes()));
        WorldBossStateRecord updated = worldBossRepository.applyDamage(boss.getId(), totalDamage, now, nextAvailableAt);
        if (updated != null && "killed".equals(updated.getStatus()) && !updated.isRewardsSent()) {
            if (worldBossRepository.markRewardsSent(boss.getId())) {
                sendRewardMails(boss);
            }
        }
    }

    private WorldBossResponse toResponse(WorldBossConfig boss, WorldBossStateRecord state,
                                         LocalDateTime now, int rankLimit) {
        MapConfig map = gameConfigService.getMapRequired(boss.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(boss.getMonsterId());
        WorldBossResponse response = new WorldBossResponse();
        response.setId(boss.getId());
        response.setName(boss.getName());
        response.setMapId(map.getId());
        response.setMapName(map.getName());
        response.setMonsterId(monster.getId());
        response.setMonsterName(monster.getName());
        response.setRequiredLevel(Math.max(1, boss.getRequiredLevel()));
        response.setRequiredPower(Math.max(0, boss.getRequiredPower()));
        response.setRewardMultiplier(Math.max(1, boss.getRewardMultiplier()));
        response.setActivityRewardBonusPercent(activityEffectService.activePercent(ActivityEffectService.WORLD_BOSS_GOLD));
        response.setRespawnMinutes(Math.max(1, boss.getRespawnMinutes()));
        response.setStatus(state.getStatus());
        response.setAvailable(isAvailable(state, now));
        response.setCurrentHp(state.getCurrentHp());
        response.setMaxHp(state.getMaxHp());
        response.setAvailableAt(FORMATTER.format(state.getAvailableAt()));
        response.setRanks(worldBossRepository.listRanks(boss.getId(), rankLimit));
        return response;
    }

    private void validateRequirements(PlayerCharacter character, WorldBossConfig boss) {
        if (character.getLevel() < Math.max(1, boss.getRequiredLevel())) {
            throw new BusinessException("WORLD_BOSS_LEVEL_NOT_ENOUGH", "角色等级不足，无法挑战世界 BOSS");
        }
        if (character.getPower() < Math.max(0, boss.getRequiredPower())) {
            throw new BusinessException("WORLD_BOSS_POWER_NOT_ENOUGH", "角色战力不足，无法挑战世界 BOSS");
        }
    }

    private boolean isAvailable(WorldBossStateRecord state, LocalDateTime now) {
        return state != null
                && "available".equals(state.getStatus())
                && state.getCurrentHp() > 0
                && !state.getAvailableAt().isAfter(now);
    }

    private int maxHp(WorldBossConfig boss) {
        MonsterConfig monster = gameConfigService.getMonsterRequired(boss.getMonsterId());
        return monster.getHp() * Math.max(3, boss.getRewardMultiplier());
    }

    private int calculatePlayerDamage(List<BattleActionResponse> actions) {
        int damage = 0;
        if (actions == null) {
            return damage;
        }
        for (BattleActionResponse action : actions) {
            if ("player".equals(action.getActor()) && "attack".equals(action.getAction())) {
                damage += Math.max(0, action.getDamage());
            }
        }
        return damage;
    }

    private void sendRewardMails(WorldBossConfig boss) {
        MonsterConfig monster = gameConfigService.getMonsterRequired(boss.getMonsterId());
        int baseGold = Math.max(1, monster.getGoldMax()) * Math.max(1, boss.getRewardMultiplier());
        for (WorldBossDamageRankResponse rank : worldBossRepository.listRanks(boss.getId(), RANK_LIMIT)) {
            int rankGold = baseGold * rankCoefficient(rank.getRank());
            int gold = rankGold + activityEffectService.bonusAmount(rankGold, ActivityEffectService.WORLD_BOSS_GOLD);
            String title = "世界 BOSS 奖励：" + boss.getName();
            String content = "你在本轮世界 BOSS 中排名第 " + rank.getRank()
                    + "，累计伤害 " + rank.getDamage() + "，奖励已通过邮件发放。";
            mailService.createMail(rank.getCharacterId(), title, content, gold,
                    null, 0, "world_boss", boss.getId());
        }
    }

    private int rankCoefficient(int rank) {
        if (rank == 1) {
            return 5;
        }
        if (rank == 2) {
            return 3;
        }
        if (rank == 3) {
            return 2;
        }
        return 1;
    }

    private PlayerCharacter requireCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }
}
