package com.paly.legend.idle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.paly.legend.activity.ActivityEffectService;
import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterProgression;
import com.paly.legend.character.CharacterProgressionResult;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.DropItemConfig;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MonsterConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.task.TaskRewardItemResponse;
import com.paly.legend.task.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdleService {

    private static final long MAX_SECONDS = 8 * 60 * 60;

    private final IdleRepository idleRepository;
    private final CharacterRepository characterRepository;
    private final CharacterProgression characterProgression;
    private final GameConfigService gameConfigService;
    private final BattleRepository battleRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryCapacityService inventoryCapacityService;
    private final TaskService taskService;
    private final ActivityEffectService activityEffectService;

    public IdleService(IdleRepository idleRepository,
                       CharacterRepository characterRepository,
                       CharacterProgression characterProgression,
                       GameConfigService gameConfigService,
                       BattleRepository battleRepository,
                       InventoryRepository inventoryRepository,
                       InventoryGrantService inventoryGrantService,
                       InventoryCapacityService inventoryCapacityService,
                       TaskService taskService,
                       ActivityEffectService activityEffectService) {
        this.idleRepository = idleRepository;
        this.characterRepository = characterRepository;
        this.characterProgression = characterProgression;
        this.gameConfigService = gameConfigService;
        this.battleRepository = battleRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryCapacityService = inventoryCapacityService;
        this.taskService = taskService;
        this.activityEffectService = activityEffectService;
    }

    @Transactional
    public IdleStatusResponse start(CurrentUser currentUser, IdleStartRequest request) {
        PlayerCharacter character = getCharacter(currentUser);
        MapConfig map = gameConfigService.getMapRequired(request.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(request.getMonsterId());
        validateCanIdle(character, map, monster);
        idleRepository.upsert(character.getId(), map.getId(), monster.getId(), LocalDateTime.now());
        return status(currentUser);
    }

    public IdleStatusResponse status(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        IdleSessionRecord record = idleRepository.findByCharacterId(character.getId());
        if (record == null) {
            IdleStatusResponse response = new IdleStatusResponse();
            response.setActive(false);
            return response;
        }
        return buildStatus(record, LocalDateTime.now());
    }

    @Transactional
    public IdleClaimResponse claim(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        IdleSessionRecord record = idleRepository.findByCharacterId(character.getId());
        if (record == null) {
            throw new BusinessException("IDLE_NOT_STARTED", "尚未开始挂机");
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = cappedSeconds(record, now);
        if (seconds < 60) {
            throw new BusinessException("IDLE_TOO_SHORT", "挂机时间不足 1 分钟，暂不可领取");
        }
        MonsterConfig monster = gameConfigService.getMonsterRequired(record.getMonsterId());
        int minutes = (int) (seconds / 60);
        int baseExp = Math.max(1, monster.getExp() * minutes / 4);
        int baseGold = Math.max(1, averageGold(monster) * minutes / 4);
        int bonusExp = activityEffectService.bonusAmount(baseExp, ActivityEffectService.IDLE_EXP);
        int bonusGold = activityEffectService.bonusAmount(baseGold, ActivityEffectService.IDLE_GOLD);
        int expGained = baseExp + bonusExp;
        int goldGained = baseGold + bonusGold;
        inventoryCapacityService.requireSpaceFor(character.getId(), estimateIdleItemGrants(monster.getId(), seconds));
        int beforeGold = character.getGold();
        CharacterProgressionResult progression = characterProgression.applyBattleReward(character, expGained, goldGained);
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
        battleRepository.createCurrencyLog(
                character.getId(),
                goldGained,
                beforeGold,
                progression.getGold(),
                "idle_reward",
                monster.getId());
        List<TaskRewardItemResponse> items = grantIdleItems(character.getId(), monster.getId(), seconds);
        idleRepository.updateLastClaimedAt(character.getId(), now);
        taskService.onLevelChanged(character.getId(), progression.getLevel());

        IdleClaimResponse response = new IdleClaimResponse();
        response.setExpGained(expGained);
        response.setGoldGained(goldGained);
        response.setBonusExp(bonusExp);
        response.setBonusGold(bonusGold);
        response.setLevelBefore(character.getLevel());
        response.setLevelAfter(progression.getLevel());
        response.setCurrentExp(progression.getExp());
        response.setCurrentGold(progression.getGold());
        response.setPower(progression.getPower());
        response.setSettledSeconds(seconds);
        response.setItems(items);
        return response;
    }

    private IdleStatusResponse buildStatus(IdleSessionRecord record, LocalDateTime now) {
        MapConfig map = gameConfigService.getMapRequired(record.getMapId());
        MonsterConfig monster = gameConfigService.getMonsterRequired(record.getMonsterId());
        long elapsed = Math.max(0, Duration.between(record.getLastClaimedAt(), now).getSeconds());
        long capped = Math.min(MAX_SECONDS, elapsed);
        int minutes = (int) (capped / 60);
        IdleStatusResponse response = new IdleStatusResponse();
        response.setActive(true);
        response.setMapId(map.getId());
        response.setMapName(map.getName());
        response.setMonsterId(monster.getId());
        response.setMonsterName(monster.getName());
        response.setElapsedSeconds(elapsed);
        response.setCappedSeconds(capped);
        int baseExp = minutes <= 0 ? 0 : Math.max(1, monster.getExp() * minutes / 4);
        int baseGold = minutes <= 0 ? 0 : Math.max(1, averageGold(monster) * minutes / 4);
        int bonusExp = activityEffectService.bonusAmount(baseExp, ActivityEffectService.IDLE_EXP);
        int bonusGold = activityEffectService.bonusAmount(baseGold, ActivityEffectService.IDLE_GOLD);
        response.setEstimatedExp(baseExp + bonusExp);
        response.setEstimatedGold(baseGold + bonusGold);
        response.setEstimatedBonusExp(bonusExp);
        response.setEstimatedBonusGold(bonusGold);
        response.setEstimatedItems(estimateIdleItems(monster.getId(), capped));
        return response;
    }

    private void validateCanIdle(PlayerCharacter character, MapConfig map, MonsterConfig monster) {
        if (character.getLevel() < map.getRequiredLevel()) {
            throw new BusinessException("MAP_LEVEL_NOT_ENOUGH", "角色等级不足，无法在该地图挂机");
        }
        if (map.getMonsterIds() == null || !map.getMonsterIds().contains(monster.getId())) {
            throw new BusinessException("MONSTER_NOT_IN_MAP", "该怪物不属于当前地图");
        }
    }

    private long cappedSeconds(IdleSessionRecord record, LocalDateTime now) {
        long elapsed = Math.max(0, Duration.between(record.getLastClaimedAt(), now).getSeconds());
        return Math.min(MAX_SECONDS, elapsed);
    }

    private int averageGold(MonsterConfig monster) {
        return Math.max(1, (monster.getGoldMin() + monster.getGoldMax()) / 2);
    }

    private List<TaskRewardItemResponse> estimateIdleItems(String monsterId, long seconds) {
        List<TaskRewardItemResponse> result = new ArrayList<TaskRewardItemResponse>();
        int quantity = idleItemQuantity(seconds);
        if (quantity <= 0) {
            return result;
        }
        for (DropItemConfig drop : gameConfigService.getMonsterDrops(monsterId)) {
            ItemConfig item = gameConfigService.getItemRequired(drop.getItemId());
            result.add(new TaskRewardItemResponse(item.getId(), item.getName(), quantity));
            break;
        }
        return result;
    }

    private List<InventoryItemGrant> estimateIdleItemGrants(String monsterId, long seconds) {
        List<InventoryItemGrant> result = new ArrayList<InventoryItemGrant>();
        int quantity = idleItemQuantity(seconds);
        if (quantity <= 0) {
            return result;
        }
        for (DropItemConfig drop : gameConfigService.getMonsterDrops(monsterId)) {
            result.add(new InventoryItemGrant(drop.getItemId(), quantity));
            break;
        }
        return result;
    }

    private List<TaskRewardItemResponse> grantIdleItems(long characterId, String monsterId, long seconds) {
        List<TaskRewardItemResponse> result = new ArrayList<TaskRewardItemResponse>();
        int quantity = idleItemQuantity(seconds);
        if (quantity <= 0) {
            return result;
        }
        for (DropItemConfig drop : gameConfigService.getMonsterDrops(monsterId)) {
            ItemConfig item = gameConfigService.getItemRequired(drop.getItemId());
            inventoryGrantService.addItem(characterId, item, quantity);
            inventoryRepository.createDropLog(characterId, "idle", monsterId, item.getId(), quantity);
            result.add(new TaskRewardItemResponse(item.getId(), item.getName(), quantity));
            break;
        }
        return result;
    }

    private int idleItemQuantity(long seconds) {
        return (int) Math.min(3, seconds / 3600);
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }
}
