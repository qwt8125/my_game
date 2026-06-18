package com.paly.legend.map;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterProgression;
import com.paly.legend.character.CharacterProgressionResult;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MapEventConfig;
import com.paly.legend.config.MonsterConfig;
import com.paly.legend.config.NpcConfig;
import com.paly.legend.config.TaskRewardItemConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.task.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapInteractionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final GameConfigService gameConfigService;
    private final CharacterRepository characterRepository;
    private final CharacterProgression characterProgression;
    private final InventoryRepository inventoryRepository;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryCapacityService inventoryCapacityService;
    private final TaskService taskService;
    private final BattleRepository battleRepository;
    private final MapEventStateRepository mapEventStateRepository;

    public MapInteractionService(GameConfigService gameConfigService,
                                 CharacterRepository characterRepository,
                                 CharacterProgression characterProgression,
                                 InventoryRepository inventoryRepository,
                                 InventoryGrantService inventoryGrantService,
                                 InventoryCapacityService inventoryCapacityService,
                                 TaskService taskService,
                                 BattleRepository battleRepository,
                                 MapEventStateRepository mapEventStateRepository) {
        this.gameConfigService = gameConfigService;
        this.characterRepository = characterRepository;
        this.characterProgression = characterProgression;
        this.inventoryRepository = inventoryRepository;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryCapacityService = inventoryCapacityService;
        this.taskService = taskService;
        this.battleRepository = battleRepository;
        this.mapEventStateRepository = mapEventStateRepository;
    }

    @Transactional
    public MapSceneResponse scene(CurrentUser currentUser, String mapId) {
        PlayerCharacter character = getCharacter(currentUser);
        MapConfig map = gameConfigService.getMapRequired(mapId);

        MapSceneResponse response = new MapSceneResponse();
        response.setId(map.getId());
        response.setName(map.getName());
        response.setRequiredLevel(map.getRequiredLevel());
        response.setRecommendedPower(map.getRecommendedPower());
        response.setWidth(Math.max(800, map.getWidth()));
        response.setHeight(Math.max(500, map.getHeight()));
        response.setBackgroundSprite(map.getBackgroundSprite());
        response.setLocked(character.getLevel() < map.getRequiredLevel());
        response.setPlayer(playerPosition(character, map));

        for (String monsterId : safeList(map.getMonsterIds())) {
            response.getMonsters().add(MonsterResponse.from(gameConfigService.getMonsterRequired(monsterId)));
        }
        for (NpcConfig npc : gameConfigService.listNpcsByMapId(map.getId())) {
            response.getNpcs().add(toNpcPoint(character, npc));
        }
        for (MapEventConfig event : gameConfigService.listMapEventsByMapId(map.getId())) {
            response.getEvents().add(toEventPoint(character, event));
        }
        return response;
    }

    @Transactional
    public NpcTalkResponse talk(CurrentUser currentUser, String npcId) {
        PlayerCharacter character = getCharacter(currentUser);
        NpcConfig npc = gameConfigService.getNpcRequired(npcId);
        ensureUnlocked(character, npc.getMapId(), npc.getRequiredTaskIds());
        characterRepository.updateLocation(character.getId(), npc.getMapId(), npc.getId(), npc.getX(), npc.getY());
        taskService.onNpcTalk(character.getId(), npc.getId());

        NpcTalkResponse response = new NpcTalkResponse();
        response.setNpcId(npc.getId());
        response.setNpcName(npc.getName());
        response.setMapId(npc.getMapId());
        response.setDialogueLines(npc.getDialogueLines());
        response.setTaskIds(npc.getTaskIds());
        return response;
    }

    @Transactional
    public MapEventTriggerResponse trigger(CurrentUser currentUser, String eventId) {
        PlayerCharacter character = getCharacter(currentUser);
        MapEventConfig event = gameConfigService.getMapEventRequired(eventId);
        ensureUnlocked(character, event.getMapId(), event.getRequiredTaskIds());
        LocalDateTime now = LocalDateTime.now();
        ensureEventAvailable(character, event, now);
        if ("reward".equals(event.getType())) {
            inventoryCapacityService.requireSpaceFor(character.getId(), eventRewardGrants(event));
        }
        characterRepository.updateLocation(character.getId(), event.getMapId(), event.getId(), event.getX(), event.getY());
        taskService.onExploreEvent(character.getId(), event.getId());
        markEventTriggered(character, event, now);

        MapEventTriggerResponse response = new MapEventTriggerResponse();
        response.setEventId(event.getId());
        response.setEventName(event.getName());
        response.setMapId(event.getMapId());
        response.setMessage(message(event));

        if ("monster_area".equals(event.getType()) || "random_encounter".equals(event.getType())) {
            MonsterConfig monster = pickMonster(event);
            response.setAction("battle");
            response.setMonsterId(monster.getId());
            response.setMonsterName(monster.getName());
            return response;
        }
        if ("portal".equals(event.getType())) {
            MapConfig targetMap = gameConfigService.getMapRequired(event.getTargetMapId());
            if (character.getLevel() < targetMap.getRequiredLevel()) {
                throw new BusinessException("MAP_LEVEL_NOT_ENOUGH", "角色等级不足，无法进入该地图");
            }
            characterRepository.updateLocation(character.getId(), targetMap.getId(), event.getId(), 180, 520);
            response.setAction("portal");
            response.setTargetMapId(targetMap.getId());
            response.setMessage("进入" + targetMap.getName() + "。");
            return response;
        }
        if ("reward".equals(event.getType())) {
            grantEventRewards(character, event, response);
            response.setAction("reward");
            response.setMessage(rewardMessage(event, response));
            return response;
        }

        response.setAction("message");
        return response;
    }

    private PlayerMapPositionResponse playerPosition(PlayerCharacter character, MapConfig map) {
        PlayerMapPositionResponse response = new PlayerMapPositionResponse();
        response.setMapId(map.getId());
        response.setNodeId(character.getCurrentNodeId());
        if (map.getId().equals(character.getCurrentMapId())) {
            response.setX(character.getLastX() > 0 ? character.getLastX() : 180);
            response.setY(character.getLastY() > 0 ? character.getLastY() : 520);
        } else {
            response.setX(180);
            response.setY(520);
        }
        return response;
    }

    private MapPointResponse toNpcPoint(PlayerCharacter character, NpcConfig npc) {
        MapPointResponse response = new MapPointResponse();
        response.setId(npc.getId());
        response.setName(npc.getName());
        response.setType("npc");
        response.setX(npc.getX());
        response.setY(npc.getY());
        response.setSprite(npc.getSprite());
        response.setDescription(npc.getDescription());
        response.setTaskIds(npc.getTaskIds());
        boolean locked = isLocked(character, npc.getMapId(), npc.getRequiredTaskIds());
        response.setLocked(locked);
        response.setStatusText(locked ? "未解锁" : "可对话");
        return response;
    }

    private MapPointResponse toEventPoint(PlayerCharacter character, MapEventConfig event) {
        MapPointResponse response = new MapPointResponse();
        response.setId(event.getId());
        response.setName(event.getName());
        response.setType(event.getType());
        response.setX(event.getX());
        response.setY(event.getY());
        response.setSprite(event.getSprite());
        response.setDescription(message(event));
        response.setTargetMonsterIds(event.getTargetMonsterIds());
        response.setEncounterMonsters(event.getEncounterMonsters());
        response.setTargetMapId(event.getTargetMapId());
        response.setNextEventIds(event.getNextEventIds());
        response.setEncounterMinCount(Math.max(1, event.getEncounterMinCount()));
        response.setEncounterMaxCount(Math.max(response.getEncounterMinCount(), event.getEncounterMaxCount()));
        response.setEncounterEliteChance(Math.max(0.0, Math.min(0.6, event.getEncounterEliteChance())));
        response.setEncounterIntervalSeconds(Math.max(2, event.getEncounterIntervalSeconds()));
        MapEventStateRecord state = mapEventStateRepository.find(character.getId(), event.getId());
        applyEventState(response, event, state, LocalDateTime.now());
        boolean requirementLocked = isLocked(character, event.getMapId(), event.getRequiredTaskIds());
        response.setLocked(response.isLocked() || requirementLocked);
        if (requirementLocked) {
            response.setStatusText("未解锁");
        }
        return response;
    }

    private void ensureUnlocked(PlayerCharacter character, String mapId, List<String> requiredTaskIds) {
        MapConfig map = gameConfigService.getMapRequired(mapId);
        if (character.getLevel() < map.getRequiredLevel()) {
            throw new BusinessException("MAP_LEVEL_NOT_ENOUGH", "角色等级不足，无法进入该地图");
        }
        if (!taskService.hasClaimedTasks(character.getId(), requiredTaskIds)) {
            throw new BusinessException("MAP_EVENT_LOCKED", "前置任务尚未完成");
        }
    }

    private boolean isLocked(PlayerCharacter character, String mapId, List<String> requiredTaskIds) {
        MapConfig map = gameConfigService.getMapRequired(mapId);
        return character.getLevel() < map.getRequiredLevel()
                || !taskService.hasClaimedTasks(character.getId(), requiredTaskIds);
    }

    private void ensureEventAvailable(PlayerCharacter character, MapEventConfig event, LocalDateTime now) {
        MapEventStateRecord state = mapEventStateRepository.find(character.getId(), event.getId());
        if (state == null) {
            return;
        }
        if (state.isCompleted() && !event.isRepeatable() && !isDailyReset(event)) {
            throw new BusinessException("MAP_EVENT_COMPLETED", "该事件已完成");
        }
        if (state.getNextAvailableAt() != null && state.getNextAvailableAt().isAfter(now)) {
            throw new BusinessException("MAP_EVENT_COOLDOWN", "该事件正在冷却中");
        }
    }

    private void markEventTriggered(PlayerCharacter character, MapEventConfig event, LocalDateTime now) {
        LocalDateTime nextAvailableAt = nextAvailableAt(event, now);
        boolean completed = !event.isRepeatable() && !isDailyReset(event);
        mapEventStateRepository.markTriggered(character.getId(), event.getId(), now, nextAvailableAt, completed);
    }

    private void applyEventState(MapPointResponse response, MapEventConfig event,
                                 MapEventStateRecord state, LocalDateTime now) {
        if (state == null) {
            response.setCompleted(false);
            response.setCoolingDown(false);
            response.setStatusText("可交互");
            return;
        }
        boolean completed = state.isCompleted() && !event.isRepeatable() && !isDailyReset(event);
        boolean coolingDown = state.getNextAvailableAt() != null && state.getNextAvailableAt().isAfter(now);
        response.setCompleted(completed);
        response.setCoolingDown(coolingDown);
        response.setNextAvailableAt(state.getNextAvailableAt() == null ? null : FORMATTER.format(state.getNextAvailableAt()));
        if (completed) {
            response.setStatusText("已完成");
        } else if (coolingDown) {
            response.setStatusText("冷却中");
        } else {
            response.setStatusText("可交互");
        }
        if (completed || coolingDown) {
            response.setLocked(true);
        }
    }

    private LocalDateTime nextAvailableAt(MapEventConfig event, LocalDateTime now) {
        if (isDailyReset(event)) {
            return now.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT);
        }
        if (event.getCooldownSeconds() > 0) {
            return now.plusSeconds(event.getCooldownSeconds());
        }
        return null;
    }

    private boolean isDailyReset(MapEventConfig event) {
        return "daily".equalsIgnoreCase(event.getResetType());
    }

    private void grantEventRewards(PlayerCharacter character, MapEventConfig event, MapEventTriggerResponse response) {
        int expGained = Math.max(0, event.getRewardExp());
        int goldGained = Math.max(0, event.getRewardGold());
        response.setExpGained(expGained);
        response.setGoldGained(goldGained);
        response.setLevelBefore(character.getLevel());

        if (expGained > 0 || goldGained > 0) {
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
            if (goldGained > 0) {
                battleRepository.createCurrencyLog(
                        character.getId(),
                        goldGained,
                        beforeGold,
                        progression.getGold(),
                        "map_event_reward",
                        event.getId());
            }
            if (expGained > 0) {
                taskService.onLevelChanged(character.getId(), progression.getLevel());
            }
            response.setLevelAfter(progression.getLevel());
            response.setCurrentExp(progression.getExp());
            response.setCurrentGold(progression.getGold());
            response.setPower(progression.getPower());
        } else {
            response.setLevelAfter(character.getLevel());
            response.setCurrentExp(character.getExp());
            response.setCurrentGold(character.getGold());
            response.setPower(character.getPower());
        }

        response.setItems(grantEventItems(character.getId(), event));
    }

    private List<MapEventRewardItemResponse> grantEventItems(long characterId, MapEventConfig event) {
        List<MapEventRewardItemResponse> result = new ArrayList<MapEventRewardItemResponse>();
        if (event.getRewardItems() == null) {
            return result;
        }
        for (TaskRewardItemConfig rewardItem : event.getRewardItems()) {
            int quantity = Math.max(1, rewardItem.getQuantity());
            ItemConfig item = gameConfigService.getItemRequired(rewardItem.getItemId());
            inventoryGrantService.addItem(characterId, item, quantity);
            inventoryRepository.createDropLog(characterId, "map_event", event.getId(), item.getId(), quantity);
            result.add(new MapEventRewardItemResponse(item.getId(), item.getName(), quantity));
        }
        return result;
    }

    private List<InventoryItemGrant> eventRewardGrants(MapEventConfig event) {
        List<InventoryItemGrant> result = new ArrayList<InventoryItemGrant>();
        if (event.getRewardItems() == null) {
            return result;
        }
        for (TaskRewardItemConfig rewardItem : event.getRewardItems()) {
            result.add(new InventoryItemGrant(rewardItem.getItemId(), Math.max(1, rewardItem.getQuantity())));
        }
        return result;
    }

    private String rewardMessage(MapEventConfig event, MapEventTriggerResponse response) {
        List<String> parts = new ArrayList<String>();
        if (response.getExpGained() > 0) {
            parts.add(response.getExpGained() + " 经验");
        }
        if (response.getGoldGained() > 0) {
            parts.add(response.getGoldGained() + " 金币");
        }
        for (MapEventRewardItemResponse item : response.getItems()) {
            parts.add(item.getName() + " x" + item.getQuantity());
        }
        if (parts.isEmpty()) {
            return message(event);
        }
        return message(event) + " 获得 " + join(parts, "，") + "。";
    }

    private String join(List<String> values, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private MonsterConfig pickMonster(MapEventConfig event) {
        List<String> monsterIds = event.getTargetMonsterIds();
        if (monsterIds == null || monsterIds.isEmpty()) {
            throw new BusinessException("MAP_EVENT_NO_MONSTER", "事件未配置怪物");
        }
        String monsterId = monsterIds.get(ThreadLocalRandom.current().nextInt(monsterIds.size()));
        return gameConfigService.getMonsterRequired(monsterId);
    }

    private String message(MapEventConfig event) {
        return event.getDialogue() == null || event.getDialogue().trim().isEmpty()
                ? event.getName()
                : event.getDialogue();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? new ArrayList<String>() : values;
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }
}
