package com.paly.legend.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.paly.legend.config.TaskConfig;
import com.paly.legend.config.TaskRewardConfig;
import com.paly.legend.config.TaskRewardItemConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private static final int STATUS_UNFINISHED = 0;
    private static final int STATUS_CLAIMABLE = 1;
    private static final int STATUS_CLAIMED = 2;

    private final TaskRepository taskRepository;
    private final CharacterRepository characterRepository;
    private final CharacterProgression characterProgression;
    private final GameConfigService gameConfigService;
    private final InventoryRepository inventoryRepository;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryCapacityService inventoryCapacityService;
    private final BattleRepository battleRepository;
    private final ObjectMapper objectMapper;

    public TaskService(TaskRepository taskRepository,
                       CharacterRepository characterRepository,
                       CharacterProgression characterProgression,
                       GameConfigService gameConfigService,
                       InventoryRepository inventoryRepository,
                       InventoryGrantService inventoryGrantService,
                       InventoryCapacityService inventoryCapacityService,
                       BattleRepository battleRepository,
                       ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.characterRepository = characterRepository;
        this.characterProgression = characterProgression;
        this.gameConfigService = gameConfigService;
        this.inventoryRepository = inventoryRepository;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryCapacityService = inventoryCapacityService;
        this.battleRepository = battleRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<TaskResponse> list(CurrentUser currentUser) {
        PlayerCharacter character = getCharacter(currentUser);
        Map<String, TaskProgressRecord> progressByTaskId = loadProgress(character.getId());
        List<TaskResponse> result = new ArrayList<TaskResponse>();
        for (TaskConfig task : gameConfigService.listTasks()) {
            if (!isTaskUnlocked(task, progressByTaskId)) {
                result.add(toLockedResponse(task, character));
                continue;
            }
            TaskProgressRecord record = normalizeProgress(character, task, progressByTaskId.get(task.getId()));
            result.add(toResponse(task, record, character));
        }
        return result;
    }

    @Transactional
    public TaskClaimResponse claim(CurrentUser currentUser, String taskId) {
        PlayerCharacter character = getCharacter(currentUser);
        TaskConfig task = gameConfigService.getTaskRequired(taskId);
        Map<String, TaskProgressRecord> progressByTaskId = loadProgress(character.getId());
        if (!isTaskUnlocked(task, progressByTaskId)) {
            throw new BusinessException("TASK_LOCKED", "前置任务尚未完成");
        }
        TaskProgressRecord record = normalizeProgress(character, task,
                progressByTaskId.get(taskId));
        if (record.getStatus() == STATUS_CLAIMED) {
            throw new BusinessException("TASK_ALREADY_CLAIMED", "任务奖励已领取");
        }
        if (record.getStatus() != STATUS_CLAIMABLE) {
            throw new BusinessException("TASK_NOT_FINISHED", "任务尚未完成");
        }

        TaskRewardConfig rewards = task.getRewards();
        int expGained = rewards == null ? 0 : Math.max(0, rewards.getExp());
        int goldGained = rewards == null ? 0 : Math.max(0, rewards.getGold());
        inventoryCapacityService.requireSpaceFor(character.getId(), rewardGrants(task));
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
                    "task_reward",
                    task.getId());
        }

        List<TaskRewardItemResponse> rewardItems = grantItems(character.getId(), task);
        taskRepository.update(character.getId(), task.getId(), STATUS_CLAIMED, record.getProgressJson());
        refreshLevelTasks(character.getId(), progression.getLevel());

        TaskClaimResponse response = new TaskClaimResponse();
        response.setTaskId(task.getId());
        response.setExpGained(expGained);
        response.setGoldGained(goldGained);
        response.setLevelBefore(character.getLevel());
        response.setLevelAfter(progression.getLevel());
        response.setCurrentExp(progression.getExp());
        response.setCurrentGold(progression.getGold());
        response.setPower(progression.getPower());
        response.setItems(rewardItems);
        return response;
    }

    public void onBattleWin(long characterId, String monsterId, int currentLevel) {
        Map<String, TaskProgressRecord> progressByTaskId = loadProgress(characterId);
        for (TaskConfig task : gameConfigService.listTasks()) {
            if (!isTaskUnlocked(task, progressByTaskId)) {
                continue;
            }
            TaskProgressRecord record = progressByTaskId.get(task.getId());
            if (record != null && record.getStatus() == STATUS_CLAIMED) {
                continue;
            }
            if ("kill_monster".equals(task.getType()) && monsterId.equals(task.getTargetId())) {
                TaskProgressData data = readProgress(record == null ? null : record.getProgressJson());
                int targetCount = Math.max(1, task.getTargetCount());
                data.setCount(Math.min(targetCount, data.getCount() + 1));
                int status = data.getCount() >= targetCount ? STATUS_CLAIMABLE : STATUS_UNFINISHED;
                taskRepository.save(characterId, task.getId(), status, toJson(data));
                progressByTaskId.put(task.getId(), taskRepository.findByCharacterIdAndTaskId(characterId, task.getId()));
            } else if ("level_reach".equals(task.getType())) {
                updateLevelTask(characterId, task, record, currentLevel);
                progressByTaskId.put(task.getId(), taskRepository.findByCharacterIdAndTaskId(characterId, task.getId()));
            }
        }
    }

    public void onLevelChanged(long characterId, int currentLevel) {
        refreshLevelTasks(characterId, currentLevel);
    }

    public void onNpcTalk(long characterId, String npcId) {
        updateTargetTask(characterId, "talk_npc", npcId);
    }

    public void onExploreEvent(long characterId, String eventId) {
        updateTargetTask(characterId, "explore_event", eventId);
    }

    public boolean hasClaimedTasks(long characterId, List<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return true;
        }
        Map<String, TaskProgressRecord> progressByTaskId = loadProgress(characterId);
        for (String taskId : taskIds) {
            TaskProgressRecord record = progressByTaskId.get(taskId);
            if (record == null || record.getStatus() != STATUS_CLAIMED) {
                return false;
            }
        }
        return true;
    }

    private void refreshLevelTasks(long characterId, int currentLevel) {
        Map<String, TaskProgressRecord> progressByTaskId = loadProgress(characterId);
        for (TaskConfig task : gameConfigService.listTasks()) {
            if (!"level_reach".equals(task.getType())) {
                continue;
            }
            if (!isTaskUnlocked(task, progressByTaskId)) {
                continue;
            }
            TaskProgressRecord record = progressByTaskId.get(task.getId());
            if (record != null && record.getStatus() == STATUS_CLAIMED) {
                continue;
            }
            updateLevelTask(characterId, task, record, currentLevel);
            progressByTaskId.put(task.getId(), taskRepository.findByCharacterIdAndTaskId(characterId, task.getId()));
        }
    }

    private void updateTargetTask(long characterId, String type, String targetId) {
        Map<String, TaskProgressRecord> progressByTaskId = loadProgress(characterId);
        for (TaskConfig task : gameConfigService.listTasks()) {
            if (!type.equals(task.getType()) || !targetId.equals(task.getTargetId())) {
                continue;
            }
            if (!isTaskUnlocked(task, progressByTaskId)) {
                continue;
            }
            TaskProgressRecord record = progressByTaskId.get(task.getId());
            if (record != null && record.getStatus() == STATUS_CLAIMED) {
                continue;
            }
            TaskProgressData data = readProgress(record == null ? null : record.getProgressJson());
            int targetCount = Math.max(1, task.getTargetCount());
            data.setCount(Math.min(targetCount, Math.max(data.getCount(), 0) + 1));
            int status = data.getCount() >= targetCount ? STATUS_CLAIMABLE : STATUS_UNFINISHED;
            taskRepository.save(characterId, task.getId(), status, toJson(data));
            progressByTaskId.put(task.getId(), taskRepository.findByCharacterIdAndTaskId(characterId, task.getId()));
        }
    }

    private void updateLevelTask(long characterId, TaskConfig task, TaskProgressRecord record, int currentLevel) {
        TaskProgressData data = readProgress(record == null ? null : record.getProgressJson());
        data.setCount(currentLevel);
        int status = currentLevel >= Math.max(1, task.getTargetLevel()) ? STATUS_CLAIMABLE : STATUS_UNFINISHED;
        taskRepository.save(characterId, task.getId(), status, toJson(data));
    }

    private TaskProgressRecord normalizeProgress(PlayerCharacter character, TaskConfig task, TaskProgressRecord record) {
        TaskProgressData data = readProgress(record == null ? null : record.getProgressJson());
        int status = record == null ? STATUS_UNFINISHED : record.getStatus();
        if (status != STATUS_CLAIMED && "level_reach".equals(task.getType())) {
            data.setCount(character.getLevel());
            if (character.getLevel() >= Math.max(1, task.getTargetLevel())) {
                status = STATUS_CLAIMABLE;
            }
        }
        String progressJson = toJson(data);
        if (record == null) {
            taskRepository.insert(character.getId(), task.getId(), status, progressJson);
            record = taskRepository.findByCharacterIdAndTaskId(character.getId(), task.getId());
        } else if (record.getStatus() != status || !progressJson.equals(record.getProgressJson())) {
            taskRepository.update(character.getId(), task.getId(), status, progressJson);
            record.setStatus(status);
            record.setProgressJson(progressJson);
        }
        return record;
    }

    private List<TaskRewardItemResponse> grantItems(long characterId, TaskConfig task) {
        List<TaskRewardItemResponse> result = new ArrayList<TaskRewardItemResponse>();
        if (task.getRewards() == null || task.getRewards().getItems() == null) {
            return result;
        }
        for (TaskRewardItemConfig rewardItem : task.getRewards().getItems()) {
            int quantity = Math.max(1, rewardItem.getQuantity());
            ItemConfig item = gameConfigService.getItemRequired(rewardItem.getItemId());
            inventoryGrantService.addItem(characterId, item, quantity);
            inventoryRepository.createDropLog(characterId, "task", task.getId(), item.getId(), quantity);
            result.add(new TaskRewardItemResponse(item.getId(), item.getName(), quantity));
        }
        return result;
    }

    private List<InventoryItemGrant> rewardGrants(TaskConfig task) {
        List<InventoryItemGrant> result = new ArrayList<InventoryItemGrant>();
        if (task.getRewards() == null || task.getRewards().getItems() == null) {
            return result;
        }
        for (TaskRewardItemConfig rewardItem : task.getRewards().getItems()) {
            result.add(new InventoryItemGrant(rewardItem.getItemId(), Math.max(1, rewardItem.getQuantity())));
        }
        return result;
    }

    private TaskResponse toResponse(TaskConfig task, TaskProgressRecord record, PlayerCharacter character) {
        TaskProgressData data = readProgress(record.getProgressJson());
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setStory(task.getStory());
        response.setGuide(task.getGuide());
        response.setType(task.getType());
        response.setTargetId(task.getTargetId());
        response.setTargetCount(Math.max(1, task.getTargetCount()));
        response.setTargetLevel(task.getTargetLevel());
        response.setCurrentCount(currentCount(task, data, character));
        response.setStatus(record.getStatus());
        response.setStatusText(statusText(record.getStatus()));
        response.setTargetName(targetName(task));
        fillTargetLocation(response, task);
        response.setLocked(false);
        response.setPreTaskIds(task.getPreTaskIds());
        response.setRewards(toRewardResponse(task.getRewards()));
        return response;
    }

    private TaskResponse toLockedResponse(TaskConfig task, PlayerCharacter character) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setStory(task.getStory());
        response.setGuide(task.getGuide());
        response.setType(task.getType());
        response.setTargetId(task.getTargetId());
        response.setTargetCount(Math.max(1, task.getTargetCount()));
        response.setTargetLevel(task.getTargetLevel());
        response.setCurrentCount(0);
        response.setStatus(STATUS_UNFINISHED);
        response.setStatusText("未解锁");
        response.setLocked(true);
        response.setPreTaskIds(task.getPreTaskIds());
        response.setTargetName(targetName(task));
        fillTargetLocation(response, task);
        response.setRewards(toRewardResponse(task.getRewards()));
        return response;
    }

    private void fillTargetLocation(TaskResponse response, TaskConfig task) {
        if ("talk_npc".equals(task.getType())) {
            NpcConfig npc = gameConfigService.getNpcRequired(task.getTargetId());
            response.setTargetMapId(npc.getMapId());
            response.setTargetPointId(npc.getId());
            response.setTargetPointType("npc");
            return;
        }
        if ("explore_event".equals(task.getType())) {
            MapEventConfig event = gameConfigService.getMapEventRequired(task.getTargetId());
            response.setTargetMapId(event.getMapId());
            response.setTargetPointId(event.getId());
            response.setTargetPointType(event.getType());
            return;
        }
        if ("kill_monster".equals(task.getType())) {
            MapEventConfig event = findMonsterEvent(task.getTargetId());
            if (event != null) {
                response.setTargetMapId(event.getMapId());
                response.setTargetPointId(event.getId());
                response.setTargetPointType(event.getType());
                return;
            }
            MapConfig map = findMonsterMap(task.getTargetId());
            if (map != null) {
                response.setTargetMapId(map.getId());
                response.setTargetPointType("monster");
            }
        }
    }

    private MapEventConfig findMonsterEvent(String monsterId) {
        for (MapConfig map : gameConfigService.listMaps()) {
            for (MapEventConfig event : gameConfigService.listMapEventsByMapId(map.getId())) {
                if (event.getTargetMonsterIds() != null && event.getTargetMonsterIds().contains(monsterId)) {
                    return event;
                }
            }
        }
        return null;
    }

    private MapConfig findMonsterMap(String monsterId) {
        for (MapConfig map : gameConfigService.listMaps()) {
            if (map.getMonsterIds() != null && map.getMonsterIds().contains(monsterId)) {
                return map;
            }
        }
        return null;
    }

    private int currentCount(TaskConfig task, TaskProgressData data, PlayerCharacter character) {
        if ("level_reach".equals(task.getType())) {
            return character.getLevel();
        }
        return data.getCount();
    }

    private String targetName(TaskConfig task) {
        if ("kill_monster".equals(task.getType())) {
            MonsterConfig monster = gameConfigService.getMonsterRequired(task.getTargetId());
            return monster.getName();
        }
        if ("talk_npc".equals(task.getType())) {
            NpcConfig npc = gameConfigService.getNpcRequired(task.getTargetId());
            return "与" + npc.getName() + "对话";
        }
        if ("explore_event".equals(task.getType())) {
            return gameConfigService.getMapEventRequired(task.getTargetId()).getName();
        }
        if ("level_reach".equals(task.getType())) {
            return "达到 " + task.getTargetLevel() + " 级";
        }
        return task.getTargetId();
    }

    private boolean isTaskUnlocked(TaskConfig task, Map<String, TaskProgressRecord> progressByTaskId) {
        if (task.getPreTaskIds() == null || task.getPreTaskIds().isEmpty()) {
            return true;
        }
        for (String preTaskId : task.getPreTaskIds()) {
            TaskProgressRecord preRecord = progressByTaskId.get(preTaskId);
            if (preRecord == null || preRecord.getStatus() != STATUS_CLAIMED) {
                return false;
            }
        }
        return true;
    }

    private TaskRewardResponse toRewardResponse(TaskRewardConfig rewards) {
        TaskRewardResponse response = new TaskRewardResponse();
        if (rewards == null) {
            return response;
        }
        response.setExp(Math.max(0, rewards.getExp()));
        response.setGold(Math.max(0, rewards.getGold()));
        List<TaskRewardItemResponse> items = new ArrayList<TaskRewardItemResponse>();
        if (rewards.getItems() != null) {
            for (TaskRewardItemConfig rewardItem : rewards.getItems()) {
                ItemConfig item = gameConfigService.getItemRequired(rewardItem.getItemId());
                items.add(new TaskRewardItemResponse(item.getId(), item.getName(), Math.max(1, rewardItem.getQuantity())));
            }
        }
        response.setItems(items);
        return response;
    }

    private Map<String, TaskProgressRecord> loadProgress(long characterId) {
        Map<String, TaskProgressRecord> result = new LinkedHashMap<String, TaskProgressRecord>();
        for (TaskProgressRecord record : taskRepository.findByCharacterId(characterId)) {
            result.put(record.getTaskId(), record);
        }
        return result;
    }

    private TaskProgressData readProgress(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new TaskProgressData();
        }
        try {
            return objectMapper.readValue(json, TaskProgressData.class);
        } catch (IOException ex) {
            return new TaskProgressData();
        }
    }

    private String toJson(TaskProgressData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize task progress", ex);
        }
    }

    private String statusText(int status) {
        if (status == STATUS_CLAIMABLE) {
            return "可领取";
        }
        if (status == STATUS_CLAIMED) {
            return "已领取";
        }
        return "进行中";
    }

    private PlayerCharacter getCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }
}
