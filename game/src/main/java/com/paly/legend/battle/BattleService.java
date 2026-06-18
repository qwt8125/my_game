package com.paly.legend.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.activity.ActivityEffectService;
import com.paly.legend.boss.BossRepository;
import com.paly.legend.battleprep.BattlePreparation;
import com.paly.legend.battleprep.BattlePreparationRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.CharacterProgression;
import com.paly.legend.character.CharacterProgressionResult;
import com.paly.legend.character.CharacterStatService;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.BossConfig;
import com.paly.legend.config.DropItemConfig;
import com.paly.legend.config.EncounterMonsterConfig;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MapEventConfig;
import com.paly.legend.config.MonsterConfig;
import com.paly.legend.config.SkillConfig;
import com.paly.legend.config.WorldBossConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.mail.MailService;
import com.paly.legend.skill.CharacterSkillRecord;
import com.paly.legend.skill.SkillRepository;
import com.paly.legend.task.TaskService;
import com.paly.legend.worldboss.WorldBossService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BattleService {

    private static final int MAX_ROUNDS = 50;
    private static final int MIN_ACTION_INTERVAL_MS = 700;

    private final CharacterRepository characterRepository;
    private final CharacterProgression characterProgression;
    private final CharacterStatService characterStatService;
    private final GameConfigService gameConfigService;
    private final BattleRepository battleRepository;
    private final BattleSessionStore battleSessionStore;
    private final InventoryRepository inventoryRepository;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryCapacityService inventoryCapacityService;
    private final MailService mailService;
    private final TaskService taskService;
    private final SkillRepository skillRepository;
    private final BossRepository bossRepository;
    private final BattlePreparationRepository battlePreparationRepository;
    private final ObjectProvider<WorldBossService> worldBossServiceProvider;
    private final ActivityEffectService activityEffectService;
    private final ObjectMapper objectMapper;

    public BattleService(CharacterRepository characterRepository,
                         CharacterProgression characterProgression,
                         CharacterStatService characterStatService,
                         GameConfigService gameConfigService,
                         BattleRepository battleRepository,
                         BattleSessionStore battleSessionStore,
                         InventoryRepository inventoryRepository,
                         InventoryGrantService inventoryGrantService,
                         InventoryCapacityService inventoryCapacityService,
                         MailService mailService,
                         TaskService taskService,
                         SkillRepository skillRepository,
                         BossRepository bossRepository,
                         BattlePreparationRepository battlePreparationRepository,
                         ObjectProvider<WorldBossService> worldBossServiceProvider,
                         ActivityEffectService activityEffectService,
                         ObjectMapper objectMapper) {
        this.characterRepository = characterRepository;
        this.characterProgression = characterProgression;
        this.characterStatService = characterStatService;
        this.gameConfigService = gameConfigService;
        this.battleRepository = battleRepository;
        this.battleSessionStore = battleSessionStore;
        this.inventoryRepository = inventoryRepository;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryCapacityService = inventoryCapacityService;
        this.mailService = mailService;
        this.taskService = taskService;
        this.skillRepository = skillRepository;
        this.bossRepository = bossRepository;
        this.battlePreparationRepository = battlePreparationRepository;
        this.worldBossServiceProvider = worldBossServiceProvider;
        this.activityEffectService = activityEffectService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BattleResponse fight(CurrentUser currentUser, BattleRequest request) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }

        MapConfig map = gameConfigService.getMapRequired(request.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(request.getMonsterId());
        validateCanFight(character, map, monster);

        List<BattleActionResponse> actions = new ArrayList<BattleActionResponse>();
        int playerHp = character.getHp();
        int monsterHp = monster.getHp();
        int rounds = 0;
        boolean win = false;

        for (int round = 1; round <= MAX_ROUNDS; round++) {
            rounds = round;

            int playerDamage = Math.max(1, character.getAttack() - monster.getDefense());
            monsterHp = Math.max(0, monsterHp - playerDamage);
            actions.add(new BattleActionResponse(
                    round,
                    "player",
                    "attack",
                    monster.getId(),
                    playerDamage,
                    monsterHp,
                    "你攻击了" + monster.getName() + "，造成 " + playerDamage + " 点伤害。"));

            if (monsterHp <= 0) {
                win = true;
                actions.add(new BattleActionResponse(
                        round,
                        "system",
                        "battle_end",
                        "monster",
                        0,
                        0,
                        "你击败了" + monster.getName() + "。"));
                break;
            }

            int monsterDamage = Math.max(1, monster.getAttack() - character.getDefense());
            playerHp = Math.max(0, playerHp - monsterDamage);
            actions.add(new BattleActionResponse(
                    round,
                    "monster",
                    "attack",
                    "player",
                    monsterDamage,
                    playerHp,
                    monster.getName() + "攻击了你，造成 " + monsterDamage + " 点伤害。"));

            if (playerHp <= 0) {
                actions.add(new BattleActionResponse(
                        round,
                        "system",
                        "battle_end",
                        "player",
                        0,
                        0,
                        "你被" + monster.getName() + "击败了。"));
                break;
            }
        }

        if (!win && playerHp > 0) {
            actions.add(new BattleActionResponse(
                    rounds,
                    "system",
                    "battle_end",
                    "system",
                    0,
                    0,
                    "战斗超过最大回合数，判定失败。"));
        }

        int baseExp = win ? monster.getExp() : 0;
        int baseGold = win ? randomGold(monster) : 0;
        int bonusExp = activityEffectService.bonusAmount(baseExp, ActivityEffectService.BATTLE_EXP);
        int bonusGold = activityEffectService.bonusAmount(baseGold, ActivityEffectService.BATTLE_GOLD);
        int expGained = baseExp + bonusExp;
        int goldGained = baseGold + bonusGold;
        int beforeGold = character.getGold();
        CharacterProgressionResult progression = characterProgression.applyBattleReward(character, expGained, goldGained);
        List<BattleDropResponse> drops = new ArrayList<BattleDropResponse>();

        if (win) {
            characterRepository.updateAfterBattle(
                    character.getId(),
                    progression.getLevel(),
                    progression.getExp(),
                    progression.getGold(),
                    progression.getHp(),
                    progression.getAttack(),
                    progression.getDefense(),
                    progression.getAttackSpeed(),
                    progression.getPower());
            if (goldGained > 0) {
                battleRepository.createCurrencyLog(
                        character.getId(),
                        goldGained,
                        beforeGold,
                        progression.getGold(),
                        "battle_reward",
                        monster.getId());
            }
            if (progression.getLevelUps() > 0) {
                actions.add(new BattleActionResponse(
                        rounds,
                        "system",
                        "level_up",
                        "player",
                        0,
                        playerHp,
                        "等级提升到 " + progression.getLevel() + "，属性获得成长。"));
            }
            actions.add(new BattleActionResponse(
                    rounds,
                    "system",
                    "reward",
                    "player",
                    0,
                    playerHp,
                    rewardMessage(expGained, goldGained, bonusExp, bonusGold)));
            drops = rollDrops(character.getId(), monster);
            if (!drops.isEmpty()) {
                actions.add(new BattleActionResponse(
                        rounds,
                        "system",
                        "drop",
                        "player",
                        0,
                        playerHp,
                        buildDropMessage(drops)));
            }
            taskService.onBattleWin(character.getId(), monster.getId(), progression.getLevel());
        }

        long battleLogId = battleRepository.createBattleLog(
                character.getId(),
                map.getId(),
                monster.getId(),
                win,
                rounds,
                expGained,
                goldGained,
                toJson(actions),
                toJson(drops));

        BattleResponse response = new BattleResponse();
        response.setWin(win);
        response.setRounds(rounds);
        response.setExpGained(expGained);
        response.setGoldGained(goldGained);
        response.setBonusExp(bonusExp);
        response.setBonusGold(bonusGold);
        response.setBattleLogId(battleLogId);
        response.setLevelBefore(character.getLevel());
        response.setLevelAfter(progression.getLevel());
        response.setLevelUps(progression.getLevelUps());
        response.setCurrentExp(progression.getExp());
        response.setCurrentGold(progression.getGold());
        response.setHp(progression.getHp());
        response.setAttack(progression.getAttack());
        response.setDefense(progression.getDefense());
        response.setAttackSpeed(progression.getAttackSpeed());
        response.setPower(progression.getPower());
        response.setActions(actions);
        response.setDrops(drops);
        return response;
    }

    @Transactional
    public BattleSessionResponse start(CurrentUser currentUser, BattleRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        MapConfig map = gameConfigService.getMapRequired(request.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(request.getMonsterId());
        validateCanFight(character, map, monster);
        return startSession(character, map, monster, "normal", null, 1);
    }

    @Transactional
    public BattleSessionResponse startEncounter(CurrentUser currentUser, BattleEncounterRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        MapConfig map = gameConfigService.getMapRequired(request.getMapId());
        MapEventConfig event = encounterEvent(request);
        if (event != null && !map.getId().equals(event.getMapId())) {
            throw new BusinessException("ENCOUNTER_MAP_MISMATCH", "Encounter event does not belong to this map");
        }
        MonsterConfig monster = pickEncounterMonster(map, event, request.getMonsterIds());
        validateCanFight(character, map, monster);

        int minCount = Math.max(1, Math.min(5, event == null ? request.getMinCount() : event.getEncounterMinCount()));
        int maxCount = Math.max(minCount, Math.min(5, event == null ? request.getMaxCount() : event.getEncounterMaxCount()));
        int count = ThreadLocalRandom.current().nextInt(minCount, maxCount + 1);
        double eliteChance = Math.max(0.0, Math.min(0.6, event == null ? request.getEliteChance() : event.getEncounterEliteChance()));
        boolean elite = ThreadLocalRandom.current().nextDouble() < eliteChance;
        int rewardMultiplier = Math.max(1, count * (elite ? 2 : 1));
        String displayName = encounterDisplayName(monster, count, elite);
        String sourceId = displayName + "|" + count + "|" + (elite ? "1" : "0");

        int monsterHp = Math.max(1, monster.getHp() * count);
        int monsterAttack = Math.max(1, (int) Math.floor(monster.getAttack() * (1.0 + 0.28 * (count - 1))));
        int monsterDefense = monster.getDefense() + Math.max(0, count - 1);
        int monsterAttackSpeed = monster.getAttackSpeed() + Math.max(0, count - 1) * 2;
        if (elite) {
            monsterHp = Math.max(1, (int) Math.floor(monsterHp * 1.8));
            monsterAttack = Math.max(1, (int) Math.floor(monsterAttack * 1.35));
            monsterDefense += 3;
            monsterAttackSpeed += 8;
        }
        return startSession(character, map, monster, "encounter", sourceId, rewardMultiplier,
                monsterHp, monsterAttack, monsterDefense, monsterAttackSpeed, displayName);
    }

    @Transactional
    public BattleSessionResponse startBoss(CurrentUser currentUser, String bossId) {
        PlayerCharacter character = getCharacter(currentUser);
        BossConfig boss = gameConfigService.getBossRequired(bossId);
        MapConfig map = gameConfigService.getMapRequired(boss.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(boss.getMonsterId());
        validateCanFight(character, map, monster);
        LocalDateTime now = LocalDateTime.now();
        bossRepository.ensure(boss.getId(), now);
        if (bossRepository.findByBossId(boss.getId()).getAvailableAt().isAfter(now)) {
            throw new BusinessException("BOSS_NOT_AVAILABLE", "BOSS 尚未刷新");
        }
        return startSession(character, map, monster, "boss", boss.getId(), Math.max(1, boss.getRewardMultiplier()));
    }

    @Transactional
    public BattleSessionResponse startWorldBoss(CurrentUser currentUser, String bossId) {
        PlayerCharacter character = getCharacter(currentUser);
        WorldBossConfig boss = gameConfigService.getWorldBossRequired(bossId);
        MapConfig map = gameConfigService.getMapRequired(boss.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(boss.getMonsterId());
        validateCanFight(character, map, monster);
        return startSession(character, map, monster, "world_boss", boss.getId(), 1);
    }

    @Transactional
    public BattleSessionResponse next(CurrentUser currentUser, long battleId) {
        return nextInternal(currentUser, battleId, null, null);
    }

    @Transactional
    public BattleSessionResponse next(CurrentUser currentUser, long battleId, BattleActionRequest request) {
        return nextInternal(currentUser, battleId, null, request == null ? null : request.getTargetId());
    }

    @Transactional
    public BattleSessionResponse castSkill(CurrentUser currentUser, long battleId, BattleSkillRequest request) {
        if (request == null || request.getSkillId() == null || request.getSkillId().trim().isEmpty()) {
            throw new BusinessException("SKILL_REQUIRED", "请选择要释放的技能");
        }
        return nextInternal(currentUser, battleId, request.getSkillId(), request.getTargetId());
    }

    private BattleSessionResponse nextInternal(CurrentUser currentUser, long battleId, String manualSkillId, String targetId) {
        PlayerCharacter character = getCharacter(currentUser);
        BattleSessionRecord session = getSessionForCharacter(battleId, character);
        if (!"running".equals(session.getStatus())) {
            return buildSessionResponse(session, null);
        }

        MonsterConfig monster = gameConfigService.getMonsterRequired(session.getMonsterId());
        List<BattleActionResponse> actions = readActions(session.getActionsJson());
        BattleSkillState skillState = readSkillState(session.getSkillStateJson());
        int round = session.getRound();
        int playerHp = session.getPlayerHp();
        ensureEnemies(session, monster, skillState);
        int monsterHp = totalEnemyHp(skillState);
        String actor = session.getNextActor();
        BattleActionResponse action;

        if ("monster".equals(actor)) {
            if (manualSkillId != null) {
                throw new BusinessException("BATTLE_NOT_PLAYER_TURN", "还没有轮到玩家行动");
            }
            action = null;
            for (BattleEnemyState enemy : skillState.getEnemies()) {
                if (!enemy.isAlive()) {
                    continue;
                }
                int damage = Math.max(1, enemy.getAttack() - session.getPlayerDefense());
                playerHp = Math.max(0, playerHp - damage);
                BattleActionResponse enemyAction = new BattleActionResponse(
                        round,
                        "monster",
                        "attack",
                        "player",
                        damage,
                        playerHp,
                        enemy.getName() + "攻击了你，造成 " + damage + " 点伤害。");
                enemyAction.setTargetType("single");
                enemyAction.getTargets().add(new BattleActionTargetResponse("player", "你", damage, playerHp));
                actions.add(enemyAction);
                action = enemyAction;
                if (playerHp <= 0) {
                    break;
                }
            }
            if (action == null) {
                action = new BattleActionResponse(round, "monster", "wait", "player", 0, playerHp, "敌方已经无法行动。");
            }
        } else {
            BattleActionResponse dotAction = applyDotIfNeeded(round, skillState);
            if (dotAction != null) {
                monsterHp = totalEnemyHp(skillState);
                actions.add(dotAction);
                if (monsterHp <= 0) {
                    actions.add(new BattleActionResponse(
                            round,
                            "system",
                            "battle_end",
                            "monster",
                            0,
                            0,
                            "你清理了全部敌人。"));
                    BattleSessionResponse result = settleSession(character, session, monster, true, round, playerHp, monsterHp, actions, skillState);
                    result.setAction(dotAction);
                    return result;
                }
            }
            tickCooldowns(skillState);
            SkillCast skillCast = manualSkillId == null
                    ? chooseSkill(character, session, skillState)
                    : requireManualSkill(character, manualSkillId, skillState);
            List<BattleEnemyState> targets;
            if (skillCast == null) {
                targets = singleTarget(skillState, targetId);
            } else {
                targets = skillTargets(skillState, skillCast.getSkill(), targetId);
                skillState.getCooldowns().put(skillCast.getSkill().getId(), Math.max(0, skillCast.getSkill().getCooldownRounds()));
                if (skillCast.getSkill().getDotRounds() > 0 && skillCast.getSkill().getDotDamage() > 0) {
                    skillState.setDotRounds(skillCast.getSkill().getDotRounds());
                    skillState.setDotDamage(skillCast.getSkill().getDotDamage() * skillCast.getLevel());
                    skillState.setDotName(skillCast.getSkill().getName());
                    if (!targets.isEmpty()) {
                        skillState.setDotTargetId(targets.get(0).getId());
                    }
                }
            }
            Map<String, Integer> hpBefore = enemyHpById(targets);
            int damage = applyPlayerDamage(session, skillCast, targets);
            monsterHp = totalEnemyHp(skillState);
            if (skillCast == null) {
                BattleEnemyState target = targets.isEmpty() ? null : targets.get(0);
                action = new BattleActionResponse(
                        round,
                        "player",
                        "attack",
                        target == null ? monster.getId() : target.getId(),
                        damage,
                        target == null ? monsterHp : target.getHp(),
                        target == null ? "你发动普通攻击，造成 " + damage + " 点伤害。"
                                : "你普通攻击" + target.getName() + "，造成 " + damage + " 点伤害。");
                action.setTargetType("single");
            } else {
                int healed = skillHeal(session, skillCast, playerHp);
                if (healed > 0) {
                    playerHp = Math.min(session.getPlayerMaxHp(), playerHp + healed);
                }
                action = new BattleActionResponse(
                        round,
                        "player",
                        "skill",
                        monster.getId(),
                        damage,
                        monsterHp,
                        buildSkillMessage(skillCast, targets, damage, healed));
                action.setTargetType(skillTargetType(skillCast.getSkill()));
            }
            action.setTargets(actionTargets(targets, hpBefore));
        }
        if (!"monster".equals(actor)) {
            actions.add(action);
        }

        boolean win = monsterHp <= 0;
        boolean lose = playerHp <= 0;
        boolean timeout = !win && !lose && round >= MAX_ROUNDS && "monster".equals(actor);
        if (win || lose || timeout) {
            String endTarget = win ? "monster" : (lose ? "player" : "system");
            String endMessage = win
                    ? "你清理了全部敌人。"
                    : (lose ? "你被敌方击败了。" : "战斗超过最大回合数，判定失败。");
            actions.add(new BattleActionResponse(
                    round,
                    "system",
                    "battle_end",
                    endTarget,
                    0,
                    win ? 0 : playerHp,
                    endMessage));
            BattleSessionResponse result = settleSession(character, session, monster, win, round, playerHp, monsterHp, actions, skillState);
            result.setAction(action);
            return result;
        }

        String nextActor;
        if ("monster".equals(actor)) {
            nextActor = "player";
            round += 1;
        } else {
            nextActor = "monster";
        }
        battleSessionStore.updateRunning(session.getId(), round, playerHp, monsterHp, nextActor, toJson(actions), toJson(skillState));
        BattleSessionRecord updated = battleSessionStore.findById(session.getId());
        BattleSessionResponse response = buildSessionResponse(updated, action);
        response.setAction(action);
        return response;
    }

    public BattleSessionResponse status(CurrentUser currentUser, long battleId) {
        PlayerCharacter character = getCharacter(currentUser);
        return buildSessionResponse(getSessionForCharacter(battleId, character), null);
    }

    private BattleSessionResponse startSession(PlayerCharacter character, MapConfig map, MonsterConfig monster,
                                               String sourceType, String sourceId, int rewardMultiplier) {
        return startSession(character, map, monster, sourceType, sourceId, rewardMultiplier,
                monster.getHp(), monster.getAttack(), monster.getDefense(), monster.getAttackSpeed(), monster.getName());
    }

    private BattleSessionResponse startSession(PlayerCharacter character, MapConfig map, MonsterConfig monster,
                                               String sourceType, String sourceId, int rewardMultiplier,
                                               int monsterHp, int monsterAttack, int monsterDefense,
                                               int monsterAttackSpeed, String monsterDisplayName) {
        BattleSessionRecord running = battleSessionStore.findRunningByCharacterId(character.getId());
        if (running != null) {
            return buildSessionResponse(running, null);
        }
        BattlePreparation preparation = battlePreparationRepository.findByCharacterId(character.getId());
        int playerHp = character.getHp();
        int playerAttack = character.getAttack();
        int playerDefense = character.getDefense();
        int playerAttackSpeed = character.getAttackSpeed();
        if (preparation != null && preparation.hasBonus()) {
            playerHp += Math.max(0, preparation.getBonusHp());
            playerAttack += Math.max(0, preparation.getBonusAttack());
            playerDefense += Math.max(0, preparation.getBonusDefense());
            playerAttackSpeed += Math.max(0, preparation.getBonusAttackSpeed());
            battlePreparationRepository.deleteByCharacterId(character.getId());
        }
        String nextActor = playerAttackSpeed >= monsterAttackSpeed ? "player" : "monster";
        List<BattleActionResponse> actions = new ArrayList<BattleActionResponse>();
        actions.add(new BattleActionResponse(
                1,
                "system",
                "battle_start",
                monster.getId(),
                0,
                monsterHp,
                "Encounter " + monsterDisplayName + ", battle starts."));
        if (preparation != null && preparation.hasBonus()) {
            actions.add(new BattleActionResponse(
                    1,
                    "system",
                    "battle_preparation",
                    "player",
                    0,
                    playerHp,
                    buildPreparationMessage(preparation)));
        }
        long battleId = battleSessionStore.create(
                character.getId(),
                map.getId(),
                monster.getId(),
                sourceType,
                sourceId,
                rewardMultiplier,
                playerHp,
                playerAttack,
                playerDefense,
                playerAttackSpeed,
                monsterHp,
                monsterAttack,
                monsterDefense,
                monsterAttackSpeed,
                nextActor,
                toJson(actions),
                toJson(initialSkillState(monster, sourceType, sourceId)));
        return buildSessionResponse(battleSessionStore.findById(battleId), null);
    }

    private String buildPreparationMessage(BattlePreparation preparation) {
        List<String> parts = new ArrayList<String>();
        if (preparation.getBonusHp() > 0) {
            parts.add("生命 +" + preparation.getBonusHp());
        }
        if (preparation.getBonusAttack() > 0) {
            parts.add("攻击 +" + preparation.getBonusAttack());
        }
        if (preparation.getBonusDefense() > 0) {
            parts.add("防御 +" + preparation.getBonusDefense());
        }
        if (preparation.getBonusAttackSpeed() > 0) {
            parts.add("攻速 +" + preparation.getBonusAttackSpeed());
        }
        return "战斗准备生效：" + String.join("，", parts) + "。";
    }

    private List<String> encounterMonsterIds(MapConfig map, List<String> requestedMonsterIds) {
        List<String> result = new ArrayList<String>();
        List<String> allowed = map.getMonsterIds() == null ? new ArrayList<String>() : map.getMonsterIds();
        List<String> source = requestedMonsterIds == null || requestedMonsterIds.isEmpty() ? allowed : requestedMonsterIds;
        for (String monsterId : source) {
            if (monsterId == null || monsterId.trim().isEmpty()) {
                continue;
            }
            String value = monsterId.trim();
            if (!allowed.contains(value)) {
                throw new BusinessException("MONSTER_NOT_IN_MAP", "该怪物不属于当前地图");
            }
            result.add(value);
        }
        if (result.isEmpty()) {
            throw new BusinessException("ENCOUNTER_NO_MONSTER", "Current map has no encounter monsters");
        }
        return result;
    }

    private MapEventConfig encounterEvent(BattleEncounterRequest request) {
        String eventId = request.getEventId();
        if (eventId == null || eventId.trim().isEmpty()) {
            return null;
        }
        MapEventConfig event = gameConfigService.getMapEventRequired(eventId.trim());
        if (!"monster_area".equals(event.getType()) && !"random_encounter".equals(event.getType())) {
            throw new BusinessException("ENCOUNTER_EVENT_INVALID", "Map event cannot start auto encounter");
        }
        return event;
    }

    private MonsterConfig pickEncounterMonster(MapConfig map, MapEventConfig event, List<String> requestedMonsterIds) {
        if (event == null || event.getEncounterMonsters() == null || event.getEncounterMonsters().isEmpty()) {
            List<String> monsterIds = encounterMonsterIds(map, event == null ? requestedMonsterIds : event.getTargetMonsterIds());
            return gameConfigService.getMonsterRequired(monsterIds.get(ThreadLocalRandom.current().nextInt(monsterIds.size())));
        }
        int totalWeight = 0;
        for (EncounterMonsterConfig config : event.getEncounterMonsters()) {
            if (config == null || config.getMonsterId() == null || config.getMonsterId().trim().isEmpty()) {
                continue;
            }
            if (map.getMonsterIds() == null || !map.getMonsterIds().contains(config.getMonsterId())) {
                throw new BusinessException("MONSTER_NOT_IN_MAP", "璇ユ€墿涓嶅睘浜庡綋鍓嶅湴鍥?");
            }
            totalWeight += Math.max(0, config.getWeight());
        }
        if (totalWeight <= 0) {
            throw new BusinessException("ENCOUNTER_WEIGHT_INVALID", "Encounter monster weights are invalid");
        }
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (EncounterMonsterConfig config : event.getEncounterMonsters()) {
            int weight = Math.max(0, config.getWeight());
            if (weight <= 0) {
                continue;
            }
            cursor += weight;
            if (roll < cursor) {
                return gameConfigService.getMonsterRequired(config.getMonsterId());
            }
        }
        return gameConfigService.getMonsterRequired(event.getEncounterMonsters().get(0).getMonsterId());
    }

    private String encounterDisplayName(MonsterConfig monster, int count, boolean elite) {
        StringBuilder builder = new StringBuilder();
        if (elite) {
            builder.append("Elite ");
        }
        builder.append(monster.getName());
        if (count > 1) {
            builder.append(" x").append(count);
        }
        return builder.toString();
    }

    private BattleSessionResponse settleSession(PlayerCharacter character, BattleSessionRecord session,
                                                MonsterConfig monster, boolean win, int round,
                                                int playerHp, int monsterHp,
                                                List<BattleActionResponse> actions,
                                                BattleSkillState skillState) {
        boolean worldBoss = "world_boss".equals(session.getSourceType());
        int sessionMultiplier = Math.max(1, session.getRewardMultiplier());
        boolean bossBattle = "boss".equals(session.getSourceType());
        boolean encounterBattle = "encounter".equals(session.getSourceType());
        int baseExp = win && !worldBoss ? monster.getExp() : 0;
        int baseGold = win && !worldBoss ? randomGold(monster) : 0;
        if (baseGold > 0) {
            baseGold += baseGold * Math.max(0, characterStatService.goldBonusPercent(character.getId())) / 100;
        }
        int multiplierBonusExp = (bossBattle || encounterBattle) ? Math.max(0, baseExp * (sessionMultiplier - 1)) : 0;
        int multiplierBonusGold = (bossBattle || encounterBattle) ? Math.max(0, baseGold * (sessionMultiplier - 1)) : 0;
        int activityBonusExp = activityEffectService.bonusAmount(baseExp, ActivityEffectService.BATTLE_EXP);
        int activityBonusGold = activityEffectService.bonusAmount(baseGold, ActivityEffectService.BATTLE_GOLD);
        int bonusExp = multiplierBonusExp + activityBonusExp;
        int bonusGold = multiplierBonusGold + activityBonusGold;
        int expGained = baseExp + bonusExp;
        int goldGained = baseGold + bonusGold;
        int beforeGold = character.getGold();
        CharacterProgressionResult progression = characterProgression.applyBattleReward(character, expGained, goldGained);
        List<BattleDropResponse> drops = new ArrayList<BattleDropResponse>();

        if (win && !worldBoss) {
            characterRepository.updateAfterBattle(
                    character.getId(),
                    progression.getLevel(),
                    progression.getExp(),
                    progression.getGold(),
                    progression.getHp(),
                    progression.getAttack(),
                    progression.getDefense(),
                    progression.getAttackSpeed(),
                    progression.getPower());
            if (goldGained > 0) {
                battleRepository.createCurrencyLog(
                        character.getId(),
                        goldGained,
                        beforeGold,
                        progression.getGold(),
                        bossBattle ? "boss_reward" : (encounterBattle ? "encounter_reward" : "battle_reward"),
                        bossBattle ? session.getSourceId() : monster.getId());
            }
            if (progression.getLevelUps() > 0) {
                actions.add(new BattleActionResponse(
                        round,
                        "system",
                        "level_up",
                        "player",
                        0,
                        playerHp,
                        "等级提升到 " + progression.getLevel() + "，属性获得成长。"));
            }
            actions.add(new BattleActionResponse(
                    round,
                    "system",
                    "reward",
                    "player",
                    0,
                    playerHp,
                    rewardMessage(expGained, goldGained, bonusExp, bonusGold)));
            drops = rollDrops(character.getId(), monster);
            if (!drops.isEmpty()) {
                actions.add(new BattleActionResponse(
                        round,
                        "system",
                        "drop",
                        "player",
                        0,
                        playerHp,
                        buildDropMessage(drops)));
            }
            taskService.onBattleWin(character.getId(), monster.getId(), progression.getLevel());
            if (bossBattle) {
                BossConfig boss = gameConfigService.getBossRequired(session.getSourceId());
                bossRepository.markKilled(
                        boss.getId(),
                        character.getId(),
                        LocalDateTime.now().plusMinutes(Math.max(1, boss.getRespawnMinutes())));
            }
        }
        if (worldBoss) {
            actions.add(new BattleActionResponse(
                    round,
                    "system",
                    "world_boss_damage",
                    "world_boss",
                    0,
                    Math.max(0, monsterHp),
                    "本次世界 BOSS 伤害已计入排行，击败后奖励将通过邮件发放。"));
        }

        long battleLogId = battleRepository.createBattleLog(
                character.getId(),
                session.getMapId(),
                monster.getId(),
                win,
                round,
                expGained,
                goldGained,
                toJson(actions),
                toJson(drops));

        BattleSessionResponse response = new BattleSessionResponse();
        response.setBattleId(session.getId());
        response.setStatus("finished");
        response.setWin(win);
        response.setRound(round);
        response.setNextActor("none");
        response.setSuggestedDelayMs(0);
        response.setExpGained(expGained);
        response.setGoldGained(goldGained);
        response.setBonusExp(bonusExp);
        response.setBonusGold(bonusGold);
        response.setLevelBefore(character.getLevel());
        response.setLevelAfter(progression.getLevel());
        response.setLevelUps(progression.getLevelUps());
        response.setCurrentExp(progression.getExp());
        response.setCurrentGold(progression.getGold());
        response.setPower(progression.getPower());
        response.setBattleLogId(battleLogId);
        response.setDrops(drops);
        response.setActions(actions);
        battleSessionStore.finish(session.getId(), round, playerHp, monsterHp, toJson(actions), toJson(skillState), toJson(response));
        if (worldBoss) {
            WorldBossService worldBossService = worldBossServiceProvider.getIfAvailable();
            if (worldBossService != null) {
                worldBossService.onBattleFinished(character.getId(), session.getSourceId(), session.getId(), actions, win);
            }
        }
        BattleSessionRecord finished = battleSessionStore.findById(session.getId());
        return buildSessionResponse(finished, null);
    }

    private BattleSessionResponse buildSessionResponse(BattleSessionRecord session, BattleActionResponse action) {
        MonsterConfig monster = gameConfigService.getMonsterRequired(session.getMonsterId());
        BattleSessionResponse response = readResult(session.getResultJson());
        if (response == null) {
            response = new BattleSessionResponse();
        }
        response.setBattleId(session.getId());
        response.setStatus(session.getStatus());
        response.setRound(session.getRound());
        response.setNextActor(session.getNextActor());
        response.setSuggestedDelayMs(actionDelay(session.getNextActor(), session));
        response.setPlayer(participant("player", "你", session.getPlayerHp(), session.getPlayerMaxHp(),
                session.getPlayerAttack(), session.getPlayerDefense(), session.getPlayerAttackSpeed()));
        response.setMonster(participant(monster.getId(), monsterDisplayName(session, monster), session.getMonsterHp(), session.getMonsterMaxHp(),
                session.getMonsterAttack(), session.getMonsterDefense(), session.getMonsterAttackSpeed()));
        BattleSkillState skillState = readSkillState(session.getSkillStateJson());
        ensureEnemies(session, monster, skillState);
        response.setEnemies(enemyResponses(skillState));
        response.setActions(readActions(session.getActionsJson()));
        response.setAction(action);
        response.setSkills(skillOptions(session));
        if (!"finished".equals(session.getStatus())) {
            response.setWin(null);
        }
        return response;
    }

    private BattleSkillState initialSkillState(MonsterConfig monster, String sourceType, String sourceId) {
        BattleSkillState state = new BattleSkillState();
        int count = "encounter".equals(sourceType) ? encounterCount(sourceId) : 1;
        boolean elite = "encounter".equals(sourceType) && encounterElite(sourceId);
        state.setEnemies(createEnemies(monster, Math.max(1, count), elite));
        return state;
    }

    private void ensureEnemies(BattleSessionRecord session, MonsterConfig monster, BattleSkillState state) {
        if (state.getEnemies() != null && !state.getEnemies().isEmpty()) {
            return;
        }
        BattleEnemyState enemy = new BattleEnemyState();
        enemy.setId("enemy_1");
        enemy.setMonsterId(monster.getId());
        enemy.setName(monsterDisplayName(session, monster));
        enemy.setRow("front");
        enemy.setElite(false);
        enemy.setHp(Math.max(0, session.getMonsterHp()));
        enemy.setMaxHp(Math.max(1, session.getMonsterMaxHp()));
        enemy.setAttack(session.getMonsterAttack());
        enemy.setDefense(session.getMonsterDefense());
        enemy.setAttackSpeed(session.getMonsterAttackSpeed());
        List<BattleEnemyState> enemies = new ArrayList<BattleEnemyState>();
        enemies.add(enemy);
        state.setEnemies(enemies);
    }

    private List<BattleEnemyState> createEnemies(MonsterConfig monster, int count, boolean elite) {
        List<BattleEnemyState> enemies = new ArrayList<BattleEnemyState>();
        for (int i = 1; i <= count; i++) {
            BattleEnemyState enemy = new BattleEnemyState();
            enemy.setId("enemy_" + i);
            enemy.setMonsterId(monster.getId());
            enemy.setName((elite ? "精英" : "") + monster.getName() + (count > 1 ? " #" + i : ""));
            enemy.setRow(i <= 2 ? "front" : "back");
            enemy.setElite(elite);
            int hp = elite ? Math.max(1, (int) Math.floor(monster.getHp() * 1.8)) : monster.getHp();
            enemy.setHp(hp);
            enemy.setMaxHp(hp);
            enemy.setAttack(elite ? Math.max(1, (int) Math.floor(monster.getAttack() * 1.35)) : monster.getAttack());
            enemy.setDefense(monster.getDefense() + (elite ? 3 : 0));
            enemy.setAttackSpeed(monster.getAttackSpeed() + (elite ? 8 : 0));
            enemies.add(enemy);
        }
        return enemies;
    }

    private int encounterCount(String sourceId) {
        String[] parts = sourceId == null ? new String[0] : sourceId.split("\\|");
        if (parts.length < 2) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(parts[1]));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private boolean encounterElite(String sourceId) {
        String[] parts = sourceId == null ? new String[0] : sourceId.split("\\|");
        return parts.length >= 3 && "1".equals(parts[2]);
    }

    private int totalEnemyHp(BattleSkillState state) {
        int total = 0;
        for (BattleEnemyState enemy : state.getEnemies()) {
            total += Math.max(0, enemy.getHp());
        }
        return total;
    }

    private List<BattleEnemyResponse> enemyResponses(BattleSkillState state) {
        List<BattleEnemyResponse> result = new ArrayList<BattleEnemyResponse>();
        BattleEnemyState current = firstAliveEnemy(state);
        String currentTargetId = current == null ? null : current.getId();
        boolean frontAlive = frontAlive(state);
        for (BattleEnemyState enemy : state.getEnemies()) {
            result.add(BattleEnemyResponse.from(enemy, currentTargetId, frontAlive));
        }
        return result;
    }

    private boolean frontAlive(BattleSkillState state) {
        for (BattleEnemyState enemy : state.getEnemies()) {
            if (enemy.isAlive() && "front".equals(enemy.getRow())) {
                return true;
            }
        }
        return false;
    }

    private BattleParticipantResponse participant(String id, String name, int hp, int maxHp,
                                                  int attack, int defense, int attackSpeed) {
        BattleParticipantResponse response = new BattleParticipantResponse();
        response.setId(id);
        response.setName(name);
        response.setHp(hp);
        response.setMaxHp(maxHp);
        response.setAttack(attack);
        response.setDefense(defense);
        response.setAttackSpeed(attackSpeed);
        return response;
    }

    private String monsterDisplayName(BattleSessionRecord session, MonsterConfig monster) {
        if (!"encounter".equals(session.getSourceType())) {
            return monster.getName();
        }
        String sourceId = session.getSourceId();
        if (sourceId == null || sourceId.trim().isEmpty()) {
            return monster.getName();
        }
        int separator = sourceId.indexOf('|');
        if (separator <= 0) {
            return sourceId;
        }
        return sourceId.substring(0, separator);
    }

    private int actionDelay(String actor, BattleSessionRecord session) {
        if ("none".equals(actor)) {
            return 0;
        }
        int speed = "monster".equals(actor) ? session.getMonsterAttackSpeed() : session.getPlayerAttackSpeed();
        return Math.max(MIN_ACTION_INTERVAL_MS, 2200 - speed * 12);
    }

    private BattleActionResponse applyDotIfNeeded(int round, BattleSkillState skillState) {
        if (skillState.getDotRounds() <= 0 || skillState.getDotDamage() <= 0) {
            return null;
        }
        BattleEnemyState target = enemyById(skillState, skillState.getDotTargetId());
        if (target == null || !target.isAlive()) {
            target = firstAliveEnemy(skillState);
        }
        if (target == null) {
            return null;
        }
        int damage = Math.max(1, skillState.getDotDamage());
        int hpAfter = Math.max(0, target.getHp() - damage);
        target.setHp(hpAfter);
        String name = skillState.getDotName() == null ? "持续伤害" : skillState.getDotName();
        skillState.setDotRounds(skillState.getDotRounds() - 1);
        if (skillState.getDotRounds() <= 0) {
            skillState.setDotDamage(0);
            skillState.setDotName(null);
            skillState.setDotTargetId(null);
        }
        BattleActionResponse action = new BattleActionResponse(
                round,
                "player",
                "skill_dot",
                target.getId(),
                damage,
                hpAfter,
                name + "继续影响" + target.getName() + "，造成 " + damage + " 点伤害。");
        action.setTargetType("single");
        action.getTargets().add(new BattleActionTargetResponse(target.getId(), target.getName(), damage, hpAfter));
        return action;
    }

    private void tickCooldowns(BattleSkillState skillState) {
        for (String skillId : new ArrayList<String>(skillState.getCooldowns().keySet())) {
            int next = Math.max(0, skillState.getCooldowns().get(skillId) - 1);
            if (next <= 0) {
                skillState.getCooldowns().remove(skillId);
            } else {
                skillState.getCooldowns().put(skillId, next);
            }
        }
    }

    private SkillCast chooseSkill(PlayerCharacter character, BattleSessionRecord session, BattleSkillState skillState) {
        double triggerBonus = characterStatService.skillTriggerBonus(character.getId());
        for (CharacterSkillRecord record : skillRepository.findByCharacterId(character.getId())) {
            SkillConfig skill = gameConfigService.getSkillRequired(record.getSkillId());
            if (!"active".equals(skill.getType())) {
                continue;
            }
            if (skillState.getCooldowns().containsKey(skill.getId())) {
                continue;
            }
            double chance = Math.min(0.85, Math.max(0.0, skill.getTriggerChance() + triggerBonus));
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                return new SkillCast(skill, Math.max(1, record.getLevel()));
            }
        }
        return null;
    }

    private SkillCast requireManualSkill(PlayerCharacter character, String skillId, BattleSkillState skillState) {
        CharacterSkillRecord record = skillRepository.findByCharacterIdAndSkillId(character.getId(), skillId);
        if (record == null) {
            throw new BusinessException("SKILL_NOT_LEARNED", "请先学习该技能");
        }
        if (record.getSkillSlot() <= 0) {
            throw new BusinessException("SKILL_NOT_IN_BAR", "请先把技能放入技能栏");
        }
        SkillConfig skill = gameConfigService.getSkillRequired(record.getSkillId());
        if (!"active".equals(skill.getType())) {
            throw new BusinessException("SKILL_NOT_ACTIVE", "被动技能不能手动释放");
        }
        if (skillState.getCooldowns().containsKey(skill.getId())) {
            throw new BusinessException("SKILL_COOLDOWN", "技能冷却中");
        }
        return new SkillCast(skill, Math.max(1, record.getLevel()), true);
    }

    private List<BattleSkillOptionResponse> skillOptions(BattleSessionRecord session) {
        BattleSkillState skillState = readSkillState(session.getSkillStateJson());
        List<BattleSkillOptionResponse> result = new ArrayList<BattleSkillOptionResponse>();
        for (CharacterSkillRecord record : skillRepository.findByCharacterId(session.getCharacterId())) {
            if (record.getSkillSlot() <= 0) {
                continue;
            }
            SkillConfig skill = gameConfigService.getSkillRequired(record.getSkillId());
            if (!"active".equals(skill.getType())) {
                continue;
            }
            BattleSkillOptionResponse response = new BattleSkillOptionResponse();
            response.setId(skill.getId());
            response.setName(skill.getName());
            response.setLevel(Math.max(1, record.getLevel()));
            response.setSkillSlot(record.getSkillSlot());
            Integer cooldown = skillState.getCooldowns().get(skill.getId());
            response.setCooldownRemaining(cooldown == null ? 0 : Math.max(0, cooldown));
            response.setReady("running".equals(session.getStatus())
                    && "player".equals(session.getNextActor())
                    && response.getCooldownRemaining() <= 0);
            response.setTargetType(skillTargetType(skill));
            response.setDescription(skill.getDescription());
            result.add(response);
        }
        return result;
    }

    private BattleEnemyState firstAliveEnemy(BattleSkillState state) {
        for (BattleEnemyState enemy : state.getEnemies()) {
            if (enemy.isAlive()) {
                return enemy;
            }
        }
        return null;
    }

    private BattleEnemyState firstSelectableEnemy(BattleSkillState state) {
        boolean frontAlive = frontAlive(state);
        for (BattleEnemyState enemy : state.getEnemies()) {
            if (enemy.isAlive() && (!frontAlive || !"back".equals(enemy.getRow()))) {
                return enemy;
            }
        }
        return firstAliveEnemy(state);
    }

    private BattleEnemyState enemyById(BattleSkillState state, String enemyId) {
        if (enemyId == null || enemyId.trim().isEmpty()) {
            return null;
        }
        for (BattleEnemyState enemy : state.getEnemies()) {
            if (enemyId.equals(enemy.getId())) {
                return enemy;
            }
        }
        return null;
    }

    private List<BattleEnemyState> singleTarget(BattleSkillState state, String targetId) {
        List<BattleEnemyState> result = new ArrayList<BattleEnemyState>();
        BattleEnemyState target = enemyById(state, targetId);
        if (target == null || !target.isAlive() || (frontAlive(state) && "back".equals(target.getRow()))) {
            target = firstSelectableEnemy(state);
        }
        if (target != null) {
            result.add(target);
        }
        return result;
    }

    private List<BattleEnemyState> skillTargets(BattleSkillState state, SkillConfig skill, String targetId) {
        String targetType = skillTargetType(skill);
        List<BattleEnemyState> alive = new ArrayList<BattleEnemyState>();
        for (BattleEnemyState enemy : state.getEnemies()) {
            if (enemy.isAlive()) {
                alive.add(enemy);
            }
        }
        if ("all".equals(targetType)) {
            return alive;
        }
        if ("front_row".equals(targetType)) {
            return rowTargets(state, "front");
        }
        if ("back_row".equals(targetType)) {
            return rowTargets(state, "back");
        }
        if ("random3".equals(targetType)) {
            List<BattleEnemyState> result = new ArrayList<BattleEnemyState>();
            List<BattleEnemyState> pool = new ArrayList<BattleEnemyState>(alive);
            while (!pool.isEmpty() && result.size() < 3) {
                result.add(pool.remove(ThreadLocalRandom.current().nextInt(pool.size())));
            }
            return result;
        }
        return singleTarget(state, targetId);
    }

    private List<BattleEnemyState> rowTargets(BattleSkillState state, String row) {
        List<BattleEnemyState> alive = new ArrayList<BattleEnemyState>();
        List<BattleEnemyState> rowTargets = new ArrayList<BattleEnemyState>();
        for (BattleEnemyState enemy : state.getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }
            alive.add(enemy);
            if (row.equals(enemy.getRow())) {
                rowTargets.add(enemy);
            }
        }
        return rowTargets.isEmpty() ? alive : rowTargets;
    }

    private int applyPlayerDamage(BattleSessionRecord session, SkillCast cast, List<BattleEnemyState> targets) {
        int total = 0;
        for (BattleEnemyState target : targets) {
            int damage = cast == null
                    ? Math.max(1, session.getPlayerAttack() - target.getDefense())
                    : skillDamage(session, cast, target);
            target.setHp(Math.max(0, target.getHp() - damage));
            total += damage;
        }
        return total;
    }

    private Map<String, Integer> enemyHpById(List<BattleEnemyState> targets) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (BattleEnemyState target : targets) {
            result.put(target.getId(), target.getHp());
        }
        return result;
    }

    private List<BattleActionTargetResponse> actionTargets(List<BattleEnemyState> targets, Map<String, Integer> hpBefore) {
        List<BattleActionTargetResponse> result = new ArrayList<BattleActionTargetResponse>();
        for (BattleEnemyState target : targets) {
            Integer before = hpBefore.get(target.getId());
            int damageTaken = Math.max(0, (before == null ? target.getHp() : before) - target.getHp());
            result.add(new BattleActionTargetResponse(target.getId(), target.getName(), damageTaken, target.getHp()));
        }
        return result;
    }

    private int skillDamage(BattleSessionRecord session, SkillCast cast, BattleEnemyState target) {
        SkillConfig skill = cast.getSkill();
        int level = cast.getLevel();
        double multiplier = skill.getDamageMultiplier() + Math.max(0, level - 1) * skill.getLevelDamageMultiplier();
        int flat = skill.getFlatDamage() + Math.max(0, level - 1) * skill.getLevelFlatDamage();
        int defense = Math.max(0, target.getDefense() - Math.max(0, skill.getDefenseBreak() * level));
        return Math.max(1, (int) Math.floor(session.getPlayerAttack() * multiplier) + flat - defense);
    }

    private int skillHeal(BattleSessionRecord session, SkillCast cast, int playerHp) {
        double multiplier = cast.getSkill().getSelfHealMultiplier();
        if (multiplier <= 0) {
            return 0;
        }
        int heal = Math.max(1, (int) Math.floor(session.getPlayerAttack() * multiplier * cast.getLevel()));
        return Math.min(heal, Math.max(0, session.getPlayerMaxHp() - playerHp));
    }

    private String buildSkillMessage(SkillCast cast, List<BattleEnemyState> targets, int damage, int healed) {
        SkillConfig skill = cast.getSkill();
        String targetText = targets.size() <= 1
                ? (targets.isEmpty() ? "目标" : targets.get(0).getName())
                : targets.size() + " 个目标";
        StringBuilder builder = new StringBuilder();
        builder.append(cast.isManual() ? "你手动施展" : "你施展").append(skill.getName()).append("，对").append(targetText)
                .append("造成 ").append(damage).append(" 点伤害");
        if (skill.getDotRounds() > 0 && skill.getDotDamage() > 0) {
            builder.append("，并附加持续伤害");
        }
        if (healed > 0) {
            builder.append("，自身恢复 ").append(healed).append(" 点生命");
        }
        builder.append("。");
        return builder.toString();
    }

    private String skillTargetType(SkillConfig skill) {
        String type = skill.getTargetType();
        if (type == null || type.trim().isEmpty()) {
            return "single";
        }
        String value = type.trim().toLowerCase();
        if ("all".equals(value) || "front_row".equals(value) || "back_row".equals(value) || "random3".equals(value)) {
            return value;
        }
        return "single";
    }

    private BattleSessionRecord getSessionForCharacter(long battleId, PlayerCharacter character) {
        BattleSessionRecord session = battleSessionStore.findById(battleId);
        if (session == null || session.getCharacterId() != character.getId()) {
            throw new BusinessException("BATTLE_NOT_FOUND", "战斗不存在");
        }
        return session;
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }

    private void validateCanFight(PlayerCharacter character, MapConfig map, MonsterConfig monster) {
        if (character.getLevel() < map.getRequiredLevel()) {
            throw new BusinessException("MAP_LEVEL_NOT_ENOUGH", "角色等级不足，无法进入该地图");
        }
        if (map.getMonsterIds() == null || !map.getMonsterIds().contains(monster.getId())) {
            throw new BusinessException("MONSTER_NOT_IN_MAP", "该怪物不属于当前地图");
        }
    }

    private int randomGold(MonsterConfig monster) {
        int min = monster.getGoldMin();
        int max = monster.getGoldMax();
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private List<BattleDropResponse> rollDrops(long characterId, MonsterConfig monster) {
        List<BattleDropResponse> drops = new ArrayList<BattleDropResponse>();
        double dropRateMultiplier = activityEffectService.rateMultiplier(ActivityEffectService.DROP_RATE);
        for (DropItemConfig dropItem : gameConfigService.getMonsterDrops(monster.getId())) {
            double effectiveRate = Math.min(1.0, Math.max(0.0, dropItem.getRate() * dropRateMultiplier));
            if (ThreadLocalRandom.current().nextDouble() >= effectiveRate) {
                continue;
            }

            int quantity = randomQuantity(dropItem);
            ItemConfig item = gameConfigService.getItemRequired(dropItem.getItemId());
            long inventoryItemId;
            if (inventoryCapacityService.hasSpaceFor(characterId,
                    Collections.singletonList(new InventoryItemGrant(item.getId(), quantity)))) {
                inventoryItemId = inventoryGrantService.addItem(characterId, item, quantity);
                inventoryRepository.createDropLog(characterId, "monster", monster.getId(), item.getId(), quantity);
            } else {
                inventoryItemId = 0;
                mailService.createMail(
                        characterId,
                        "背包已满补发：" + item.getName(),
                        "战斗击败 " + monster.getName() + " 时背包已满，掉落已转为邮件附件。",
                        0,
                        item.getId(),
                        quantity,
                        "battle_drop_overflow",
                        monster.getId());
            }
            drops.add(new BattleDropResponse(
                    inventoryItemId,
                    item.getId(),
                    item.getName(),
                    item.getType(),
                    item.getSlot(),
                    item.getQuality(),
                    quantity));
        }
        return drops;
    }

    private String rewardMessage(int expGained, int goldGained, int bonusExp, int bonusGold) {
        StringBuilder builder = new StringBuilder();
        builder.append("获得 ").append(expGained).append(" 经验和 ").append(goldGained).append(" 金币");
        if (bonusExp > 0 || bonusGold > 0) {
            builder.append("（活动加成");
            if (bonusExp > 0) {
                builder.append("经验 +").append(bonusExp);
            }
            if (bonusExp > 0 && bonusGold > 0) {
                builder.append("，");
            }
            if (bonusGold > 0) {
                builder.append("金币 +").append(bonusGold);
            }
            builder.append("）");
        }
        builder.append("。");
        return builder.toString();
    }

    private int randomQuantity(DropItemConfig dropItem) {
        int min = Math.max(1, dropItem.getMinQuantity());
        int max = Math.max(min, dropItem.getMaxQuantity());
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private String buildDropMessage(List<BattleDropResponse> drops) {
        StringBuilder builder = new StringBuilder("获得掉落：");
        for (int i = 0; i < drops.size(); i++) {
            BattleDropResponse drop = drops.get(i);
            if (i > 0) {
                builder.append("，");
            }
            builder.append(drop.getName()).append(" x").append(drop.getQuantity());
            if (drop.getInventoryItemId() <= 0) {
                builder.append("（已邮件补发）");
            }
        }
        builder.append("。");
        return builder.toString();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize battle data", ex);
        }
    }

    private List<BattleActionResponse> readActions(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<BattleActionResponse>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<BattleActionResponse>>() {
            });
        } catch (IOException ex) {
            return new ArrayList<BattleActionResponse>();
        }
    }

    private BattleSessionResponse readResult(String value) {
        if (value == null || value.trim().isEmpty() || "{}".equals(value.trim())) {
            return null;
        }
        try {
            return objectMapper.readValue(value, BattleSessionResponse.class);
        } catch (IOException ex) {
            return null;
        }
    }

    private BattleSkillState readSkillState(String value) {
        if (value == null || value.trim().isEmpty() || "{}".equals(value.trim())) {
            return new BattleSkillState();
        }
        try {
            return objectMapper.readValue(value, BattleSkillState.class);
        } catch (IOException ex) {
            return new BattleSkillState();
        }
    }
}
