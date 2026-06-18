package com.paly.legend.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.ActivityConfig;
import com.paly.legend.config.ActivityRankingRewardConfig;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.TaskRewardItemConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.ranking.RankingService;
import com.paly.legend.task.TaskRewardItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final GameConfigService gameConfigService;
    private final ActivityRepository activityRepository;
    private final CharacterRepository characterRepository;
    private final InventoryCapacityService inventoryCapacityService;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryRepository inventoryRepository;
    private final BattleRepository battleRepository;
    private final ActivityEffectService activityEffectService;
    private final RankingService rankingService;
    private final ObjectMapper objectMapper;

    public ActivityService(GameConfigService gameConfigService,
                           ActivityRepository activityRepository,
                           CharacterRepository characterRepository,
                           InventoryCapacityService inventoryCapacityService,
                           InventoryGrantService inventoryGrantService,
                           InventoryRepository inventoryRepository,
                           BattleRepository battleRepository,
                           ActivityEffectService activityEffectService,
                           RankingService rankingService,
                           ObjectMapper objectMapper) {
        this.gameConfigService = gameConfigService;
        this.activityRepository = activityRepository;
        this.characterRepository = characterRepository;
        this.inventoryCapacityService = inventoryCapacityService;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryRepository = inventoryRepository;
        this.battleRepository = battleRepository;
        this.activityEffectService = activityEffectService;
        this.rankingService = rankingService;
        this.objectMapper = objectMapper;
    }

    public List<ActivityResponse> list(CurrentUser currentUser) {
        PlayerCharacter character = requireCharacter(currentUser);
        Set<String> claimedIds = new HashSet<String>(activityRepository.claimedActivityIds(character.getId()));
        List<ActivityConfig> configs = new ArrayList<ActivityConfig>(gameConfigService.listActivities());
        Collections.sort(configs, Comparator.comparingInt(ActivityConfig::getPriority).reversed());
        List<ActivityResponse> result = new ArrayList<ActivityResponse>();
        for (ActivityConfig config : configs) {
            result.add(toResponse(config, claimedIds.contains(config.getId()), character.getId()));
        }
        return result;
    }

    @Transactional
    public ActivityClaimResponse claim(CurrentUser currentUser, String activityId) {
        PlayerCharacter character = requireCharacter(currentUser);
        ActivityConfig activity = gameConfigService.getActivityRequired(activityId);
        if (!"active".equals(activity.getStatus())) {
            throw new BusinessException("ACTIVITY_NOT_ACTIVE", "活动当前不可领取");
        }
        if (activityRepository.hasClaimed(character.getId(), activity.getId())) {
            throw new BusinessException("ACTIVITY_ALREADY_CLAIMED", "活动奖励已领取");
        }
        ActivityRankingRewardConfig rankingReward = eligibleRankingReward(activity, character.getId());
        if (activity.getRankingRewards() != null && !activity.getRankingRewards().isEmpty() && rankingReward == null) {
            throw new BusinessException("ACTIVITY_RANKING_NOT_ELIGIBLE", "当前榜单名次未达到活动奖励要求");
        }
        if (activity.getRewardGold() <= 0 && (activity.getRewardItems() == null || activity.getRewardItems().isEmpty())
                && rankingReward == null) {
            throw new BusinessException("ACTIVITY_NO_REWARD", "活动暂无可领取奖励");
        }
        inventoryCapacityService.requireSpaceFor(character.getId(), rewardGrants(activity, rankingReward));

        ActivityClaimResponse response = new ActivityClaimResponse();
        response.setActivityId(activity.getId());
        if (rankingReward != null) {
            response.setRankingType(rankingReward.getRankingType());
            response.setCurrentRank(rankingService.currentRank(rankingReward.getRankingType(), character.getId()));
        }
        int currentGold = character.getGold();
        int rewardGold = activity.getRewardGold() + (rankingReward == null ? 0 : Math.max(0, rankingReward.getRewardGold()));
        if (rewardGold > 0) {
            int afterGold = character.getGold() + rewardGold;
            characterRepository.addGold(character.getId(), rewardGold);
            battleRepository.createCurrencyLog(character.getId(), rewardGold,
                    character.getGold(), afterGold, "activity_claim", activity.getId());
            currentGold = afterGold;
            response.setGoldGained(rewardGold);
        }
        List<TaskRewardItemResponse> items = grantItems(character.getId(), activity, rankingReward);
        response.setItems(items);
        response.setCurrentGold(currentGold);
        activityRepository.markClaimed(character.getId(), activity.getId(), response.getGoldGained(), itemsJson(items));
        return response;
    }

    private PlayerCharacter requireCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_FOUND", "请先创建角色", HttpStatus.NOT_FOUND);
        }
        return character;
    }

    private ActivityResponse toResponse(ActivityConfig config, boolean claimed, long characterId) {
        ActivityResponse response = new ActivityResponse();
        response.setId(config.getId());
        response.setName(config.getName());
        response.setType(config.getType());
        response.setStatus(config.getStatus());
        response.setTag(config.getTag());
        response.setSummary(config.getSummary());
        response.setDescription(config.getDescription());
        response.setStartAt(config.getStartAt());
        response.setEndAt(config.getEndAt());
        response.setPriority(config.getPriority());
        response.setTargetView(config.getTargetView());
        response.setRewardGold(config.getRewardGold());
        response.setRewardItems(rewardItems(config));
        response.setEffects(activityEffectService.responses(config));
        response.setRankingRewards(rankingRewards(config, characterId));
        response.setClaimed(claimed);
        boolean hasDirectReward = config.getRewardGold() > 0 || !response.getRewardItems().isEmpty();
        boolean hasEligibleRankingReward = false;
        for (ActivityRankingRewardResponse rankingReward : response.getRankingRewards()) {
            if (rankingReward.isEligible()) {
                hasEligibleRankingReward = true;
                break;
            }
        }
        response.setClaimable("active".equals(config.getStatus()) && !claimed
                && (hasDirectReward || hasEligibleRankingReward)
                && (response.getRankingRewards().isEmpty() || hasEligibleRankingReward));
        return response;
    }

    private List<TaskRewardItemResponse> rewardItems(ActivityConfig activity) {
        List<TaskRewardItemResponse> result = new ArrayList<TaskRewardItemResponse>();
        if (activity.getRewardItems() == null) {
            return result;
        }
        for (TaskRewardItemConfig rewardItem : activity.getRewardItems()) {
            ItemConfig item = gameConfigService.getItemRequired(rewardItem.getItemId());
            result.add(new TaskRewardItemResponse(item.getId(), item.getName(), Math.max(1, rewardItem.getQuantity())));
        }
        return result;
    }

    private List<ActivityRankingRewardResponse> rankingRewards(ActivityConfig activity, long characterId) {
        List<ActivityRankingRewardResponse> result = new ArrayList<ActivityRankingRewardResponse>();
        if (activity.getRankingRewards() == null) {
            return result;
        }
        for (ActivityRankingRewardConfig reward : activity.getRankingRewards()) {
            int currentRank = rankingService.currentRank(reward.getRankingType(), characterId);
            ActivityRankingRewardResponse response = new ActivityRankingRewardResponse();
            response.setRankingType(reward.getRankingType());
            response.setMaxRank(Math.max(1, reward.getMaxRank()));
            response.setCurrentRank(currentRank);
            response.setEligible(currentRank > 0 && currentRank <= Math.max(1, reward.getMaxRank()));
            response.setRewardGold(Math.max(0, reward.getRewardGold()));
            response.setRewardItems(rewardItems(reward.getRewardItems()));
            response.setDescription(reward.getDescription());
            result.add(response);
        }
        return result;
    }

    private ActivityRankingRewardConfig eligibleRankingReward(ActivityConfig activity, long characterId) {
        if (activity.getRankingRewards() == null) {
            return null;
        }
        for (ActivityRankingRewardConfig reward : activity.getRankingRewards()) {
            int rank = rankingService.currentRank(reward.getRankingType(), characterId);
            if (rank > 0 && rank <= Math.max(1, reward.getMaxRank())) {
                return reward;
            }
        }
        return null;
    }

    private List<TaskRewardItemResponse> rewardItems(List<TaskRewardItemConfig> rewardItems) {
        List<TaskRewardItemResponse> result = new ArrayList<TaskRewardItemResponse>();
        if (rewardItems == null) {
            return result;
        }
        for (TaskRewardItemConfig rewardItem : rewardItems) {
            ItemConfig item = gameConfigService.getItemRequired(rewardItem.getItemId());
            result.add(new TaskRewardItemResponse(item.getId(), item.getName(), Math.max(1, rewardItem.getQuantity())));
        }
        return result;
    }

    private List<TaskRewardItemResponse> grantItems(long characterId, ActivityConfig activity,
                                                    ActivityRankingRewardConfig rankingReward) {
        List<TaskRewardItemResponse> result = new ArrayList<TaskRewardItemResponse>();
        grantItems(characterId, activity.getId(), activity.getRewardItems(), result);
        if (rankingReward != null) {
            grantItems(characterId, activity.getId(), rankingReward.getRewardItems(), result);
        }
        return result;
    }

    private void grantItems(long characterId, String activityId, List<TaskRewardItemConfig> rewardItems,
                            List<TaskRewardItemResponse> result) {
        if (rewardItems == null) {
            return;
        }
        for (TaskRewardItemConfig rewardItem : rewardItems) {
            int quantity = Math.max(1, rewardItem.getQuantity());
            ItemConfig item = gameConfigService.getItemRequired(rewardItem.getItemId());
            inventoryGrantService.addItem(characterId, item, quantity);
            inventoryRepository.createDropLog(characterId, "activity", activityId, item.getId(), quantity);
            result.add(new TaskRewardItemResponse(item.getId(), item.getName(), quantity));
        }
    }

    private List<InventoryItemGrant> rewardGrants(ActivityConfig activity, ActivityRankingRewardConfig rankingReward) {
        List<InventoryItemGrant> result = new ArrayList<InventoryItemGrant>();
        addRewardGrants(result, activity.getRewardItems());
        if (rankingReward != null) {
            addRewardGrants(result, rankingReward.getRewardItems());
        }
        return result;
    }

    private void addRewardGrants(List<InventoryItemGrant> result, List<TaskRewardItemConfig> rewardItems) {
        if (rewardItems == null) {
            return;
        }
        for (TaskRewardItemConfig rewardItem : rewardItems) {
            result.add(new InventoryItemGrant(rewardItem.getItemId(), Math.max(1, rewardItem.getQuantity())));
        }
    }

    private String itemsJson(List<TaskRewardItemResponse> items) {
        try {
            return objectMapper.writeValueAsString(items == null ? Collections.emptyList() : items);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}
