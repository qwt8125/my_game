package com.paly.legend.guild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.GuildActivityConfig;
import com.paly.legend.config.GuildConfig;
import com.paly.legend.config.GuildDonationOptionConfig;
import com.paly.legend.config.GuildShopItemConfig;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.TaskRewardItemConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.task.TaskRewardItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuildService {

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 12;
    private static final int NOTICE_MAX_LENGTH = 80;

    private final GuildRepository guildRepository;
    private final CharacterRepository characterRepository;
    private final GameConfigService gameConfigService;
    private final BattleRepository battleRepository;
    private final InventoryCapacityService inventoryCapacityService;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryRepository inventoryRepository;

    public GuildService(GuildRepository guildRepository,
                        CharacterRepository characterRepository,
                        GameConfigService gameConfigService,
                        BattleRepository battleRepository,
                        InventoryCapacityService inventoryCapacityService,
                        InventoryGrantService inventoryGrantService,
                        InventoryRepository inventoryRepository) {
        this.guildRepository = guildRepository;
        this.characterRepository = characterRepository;
        this.gameConfigService = gameConfigService;
        this.battleRepository = battleRepository;
        this.inventoryCapacityService = inventoryCapacityService;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryRepository = inventoryRepository;
    }

    public List<GuildSummaryResponse> list(int limit) {
        List<GuildSummaryResponse> responses = new ArrayList<GuildSummaryResponse>();
        for (GuildRecord record : guildRepository.list(limit)) {
            responses.add(GuildSummaryResponse.from(record));
        }
        return responses;
    }

    public List<GuildRankingEntryResponse> rankings(CurrentUser currentUser, int limit) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord member = guildRepository.findMemberByCharacterId(character.getId());
        Long myGuildId = member == null ? null : member.getGuildId();
        List<GuildRankingEntryResponse> responses = new ArrayList<GuildRankingEntryResponse>();
        int rank = 1;
        for (GuildRecord record : guildRepository.listByContribution(limit)) {
            responses.add(GuildRankingEntryResponse.from(rank++, record, myGuildId));
        }
        return responses;
    }

    public GuildDetailResponse me(CurrentUser currentUser) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildRecord guild = guildRepository.findByCharacterId(character.getId());
        if (guild == null) {
            GuildDetailResponse response = new GuildDetailResponse();
            response.setInGuild(false);
            return response;
        }
        GuildMemberRecord member = guildRepository.findMember(guild.getId(), character.getId());
        return detail(guild, member);
    }

    @Transactional
    public GuildActionResponse create(CurrentUser currentUser, CreateGuildRequest request) {
        PlayerCharacter character = requireCharacter(currentUser);
        if (guildRepository.findMemberByCharacterId(character.getId()) != null) {
            throw new BusinessException("GUILD_ALREADY_JOINED", "当前角色已经加入公会");
        }
        String name = normalizeName(request.getName());
        String notice = normalizeNotice(request.getNotice());
        if (guildRepository.nameExists(name)) {
            throw new BusinessException("GUILD_NAME_EXISTS", "公会名称已存在");
        }
        long guildId = guildRepository.create(name, notice, character.getId());
        guildRepository.addMember(guildId, character.getId(), "leader");
        guildRepository.log(guildId, character.getId(), "create", "{\"name\":\"" + escapeJson(name) + "\"}");
        GuildRecord guild = guildRepository.findById(guildId);
        return new GuildActionResponse(true, "公会创建成功", detail(guild, guildRepository.findMember(guildId, character.getId())));
    }

    @Transactional
    public GuildActionResponse join(CurrentUser currentUser, long guildId) {
        PlayerCharacter character = requireCharacter(currentUser);
        if (guildRepository.findMemberByCharacterId(character.getId()) != null) {
            throw new BusinessException("GUILD_ALREADY_JOINED", "当前角色已经加入公会");
        }
        GuildRecord guild = requireGuild(guildId);
        guildRepository.addMember(guild.getId(), character.getId(), "member");
        guildRepository.refreshMemberCount(guild.getId());
        guildRepository.log(guild.getId(), character.getId(), "join", "{}");
        return new GuildActionResponse(true, "已加入公会", detail(guildRepository.findById(guild.getId()), guildRepository.findMember(guild.getId(), character.getId())));
    }

    @Transactional
    public GuildActionResponse leave(CurrentUser currentUser) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord member = requireMember(character.getId());
        GuildRecord guild = requireGuild(member.getGuildId());
        int memberCount = guildRepository.countMembers(guild.getId());
        if ("leader".equals(member.getRole()) && memberCount > 1) {
            throw new BusinessException("GUILD_LEADER_TRANSFER_REQUIRED", "会长需要先转让会长后才能退出");
        }
        if (memberCount <= 1) {
            guildRepository.log(guild.getId(), character.getId(), "disband", "{}");
            guildRepository.deleteGuild(guild.getId());
            GuildDetailResponse empty = new GuildDetailResponse();
            empty.setInGuild(false);
            return new GuildActionResponse(true, "公会已解散", empty);
        }
        guildRepository.removeMember(guild.getId(), character.getId());
        guildRepository.refreshMemberCount(guild.getId());
        guildRepository.log(guild.getId(), character.getId(), "leave", "{}");
        GuildDetailResponse empty = new GuildDetailResponse();
        empty.setInGuild(false);
        return new GuildActionResponse(true, "已退出公会", empty);
    }

    @Transactional
    public GuildActionResponse kick(CurrentUser currentUser, long targetCharacterId) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord operator = requireMember(character.getId());
        requireLeader(operator);
        if (character.getId() == targetCharacterId) {
            throw new BusinessException("GUILD_KICK_SELF", "会长不能踢出自己");
        }
        GuildMemberRecord target = guildRepository.findMember(operator.getGuildId(), targetCharacterId);
        if (target == null) {
            throw new BusinessException("GUILD_MEMBER_NOT_FOUND", "目标成员不在当前公会");
        }
        guildRepository.removeMember(operator.getGuildId(), targetCharacterId);
        guildRepository.refreshMemberCount(operator.getGuildId());
        guildRepository.log(operator.getGuildId(), targetCharacterId, "kick", "{\"operator\":" + character.getId() + "}");
        GuildRecord guild = requireGuild(operator.getGuildId());
        return new GuildActionResponse(true, "成员已踢出", detail(guild, guildRepository.findMember(operator.getGuildId(), character.getId())));
    }

    @Transactional
    public GuildActionResponse transfer(CurrentUser currentUser, long targetCharacterId) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord operator = requireMember(character.getId());
        requireLeader(operator);
        if (character.getId() == targetCharacterId) {
            throw new BusinessException("GUILD_TRANSFER_SELF", "目标已经是会长");
        }
        GuildMemberRecord target = guildRepository.findMember(operator.getGuildId(), targetCharacterId);
        if (target == null) {
            throw new BusinessException("GUILD_MEMBER_NOT_FOUND", "目标成员不在当前公会");
        }
        guildRepository.transferLeader(operator.getGuildId(), character.getId(), targetCharacterId);
        guildRepository.log(operator.getGuildId(), targetCharacterId, "transfer_leader", "{\"from\":" + character.getId() + "}");
        GuildRecord guild = requireGuild(operator.getGuildId());
        return new GuildActionResponse(true, "会长已转让", detail(guild, guildRepository.findMember(operator.getGuildId(), character.getId())));
    }

    @Transactional
    public GuildActionResponse donate(CurrentUser currentUser, GuildDonateRequest request) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord member = requireMember(character.getId());
        GuildRecord guild = requireGuild(member.getGuildId());
        GuildDonationOptionConfig option = requireDonationOption(request == null ? null : request.getDonationId());
        int usedToday = guildRepository.donationCountToday(character.getId(), option.getId());
        if (usedToday >= option.getDailyLimit()) {
            throw new BusinessException("GUILD_DONATION_LIMIT_REACHED", "该捐献今日次数已用完");
        }
        if (character.getGold() < option.getGoldCost()) {
            throw new BusinessException("GOLD_NOT_ENOUGH", "金币不足");
        }
        int afterGold = character.getGold() - option.getGoldCost();
        characterRepository.addGold(character.getId(), -option.getGoldCost());
        battleRepository.createCurrencyLog(character.getId(), -option.getGoldCost(),
                character.getGold(), afterGold, "guild_donation", option.getId());
        guildRepository.addContribution(guild.getId(), character.getId(), option.getContribution());
        guildRepository.recordDonation(guild.getId(), character.getId(), option.getId(), option.getGoldCost(), option.getContribution());
        guildRepository.log(guild.getId(), character.getId(), "donate",
                "{\"donationId\":\"" + escapeJson(option.getId()) + "\",\"gold\":" + option.getGoldCost()
                        + ",\"contribution\":" + option.getContribution() + "}");
        return new GuildActionResponse(true, "捐献成功，贡献 +" + option.getContribution(),
                detail(requireGuild(guild.getId()), guildRepository.findMember(guild.getId(), character.getId())));
    }

    public List<GuildShopItemResponse> shop(CurrentUser currentUser) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord member = requireMember(character.getId());
        return shopResponses(member, character.getGold());
    }

    public List<GuildActivityResponse> activities(CurrentUser currentUser) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord member = requireMember(character.getId());
        GuildRecord guild = requireGuild(member.getGuildId());
        return activityResponses(guild, character.getId());
    }

    @Transactional
    public GuildShopPurchaseResponse buy(CurrentUser currentUser, GuildShopPurchaseRequest request) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord member = requireMember(character.getId());
        GuildRecord guild = requireGuild(member.getGuildId());
        GuildShopItemConfig shopItem = requireShopItem(request == null ? null : request.getShopItemId());
        int usedToday = guildRepository.purchaseCountToday(character.getId(), shopItem.getId());
        if (usedToday >= shopItem.getDailyLimit()) {
            throw new BusinessException("GUILD_SHOP_LIMIT_REACHED", "该商品今日已达购买上限");
        }
        if (member.getContribution() < shopItem.getMinContribution()) {
            throw new BusinessException("GUILD_CONTRIBUTION_NOT_ENOUGH", "当前贡献未达到购买条件");
        }
        if (member.getContribution() < shopItem.getContributionCost()) {
            throw new BusinessException("GUILD_CONTRIBUTION_COST_NOT_ENOUGH", "公会贡献不足");
        }
        if (character.getGold() < shopItem.getGoldCost()) {
            throw new BusinessException("GOLD_NOT_ENOUGH", "金币不足");
        }
        inventoryCapacityService.requireSpaceFor(character.getId(),
                Collections.singletonList(new InventoryItemGrant(shopItem.getItemId(), shopItem.getQuantity())));
        ItemConfig item = gameConfigService.getItemRequired(shopItem.getItemId());
        int afterGold = character.getGold() - shopItem.getGoldCost();
        if (shopItem.getGoldCost() > 0) {
            characterRepository.addGold(character.getId(), -shopItem.getGoldCost());
            battleRepository.createCurrencyLog(character.getId(), -shopItem.getGoldCost(),
                    character.getGold(), afterGold, "guild_shop", shopItem.getId());
        }
        if (shopItem.getContributionCost() > 0) {
            guildRepository.consumeContribution(guild.getId(), character.getId(), shopItem.getContributionCost());
        }
        inventoryGrantService.addItem(character.getId(), item, shopItem.getQuantity());
        inventoryRepository.createDropLog(character.getId(), "guild_shop", shopItem.getId(), item.getId(), shopItem.getQuantity());
        guildRepository.recordShopPurchase(guild.getId(), character.getId(), shopItem.getId(), item.getId(),
                shopItem.getQuantity(), shopItem.getContributionCost(), shopItem.getGoldCost());
        guildRepository.log(guild.getId(), character.getId(), "shop_purchase",
                "{\"shopItemId\":\"" + escapeJson(shopItem.getId()) + "\",\"itemId\":\"" + escapeJson(item.getId())
                        + "\",\"quantity\":" + shopItem.getQuantity() + "}");
        GuildMemberRecord refreshedMember = guildRepository.findMember(guild.getId(), character.getId());
        GuildShopPurchaseResponse response = new GuildShopPurchaseResponse();
        response.setSuccess(true);
        response.setMessage("购买成功");
        response.setShopItemId(shopItem.getId());
        response.setItemId(item.getId());
        response.setItemName(item.getName());
        response.setQuantity(shopItem.getQuantity());
        response.setGoldCost(shopItem.getGoldCost());
        response.setContributionCost(shopItem.getContributionCost());
        response.setCurrentGold(afterGold);
        response.setCurrentContribution(refreshedMember == null ? 0 : refreshedMember.getContribution());
        return response;
    }

    @Transactional
    public GuildActivityClaimResponse claimActivity(CurrentUser currentUser, String activityId) {
        PlayerCharacter character = requireCharacter(currentUser);
        GuildMemberRecord member = requireMember(character.getId());
        GuildRecord guild = requireGuild(member.getGuildId());
        GuildActivityConfig activity = requireActivity(activityId);
        if (guild.getTotalContribution() < activity.getTargetContribution()) {
            throw new BusinessException("GUILD_ACTIVITY_NOT_ACHIEVED", "公会活动目标尚未达成");
        }
        if (guildRepository.hasClaimedActivity(guild.getId(), character.getId(), activity.getId())) {
            throw new BusinessException("GUILD_ACTIVITY_ALREADY_CLAIMED", "该公会活动奖励已领取");
        }
        List<InventoryItemGrant> grants = rewardGrants(activity);
        inventoryCapacityService.requireSpaceFor(character.getId(), grants);
        int beforeGold = character.getGold();
        int afterGold = beforeGold + activity.getRewardGold();
        if (activity.getRewardGold() > 0) {
            characterRepository.addGold(character.getId(), activity.getRewardGold());
            battleRepository.createCurrencyLog(character.getId(), activity.getRewardGold(),
                    beforeGold, afterGold, "guild_activity", activity.getId());
        }
        List<TaskRewardItemResponse> items = rewardItemResponses(activity);
        for (InventoryItemGrant grant : grants) {
            ItemConfig item = gameConfigService.getItemRequired(grant.getItemId());
            inventoryGrantService.addItem(character.getId(), item, grant.getQuantity());
            inventoryRepository.createDropLog(character.getId(), "guild_activity", activity.getId(), item.getId(), grant.getQuantity());
        }
        guildRepository.recordActivityClaim(guild.getId(), character.getId(), activity.getId(),
                activity.getRewardGold(), rewardItemsJson(items));
        guildRepository.log(guild.getId(), character.getId(), "activity_claim",
                "{\"activityId\":\"" + escapeJson(activity.getId()) + "\",\"rewardGold\":" + activity.getRewardGold() + "}");
        GuildActivityClaimResponse response = new GuildActivityClaimResponse();
        response.setSuccess(true);
        response.setMessage("公会活动奖励领取成功");
        response.setActivityId(activity.getId());
        response.setGoldGained(activity.getRewardGold());
        response.setCurrentGold(afterGold);
        response.setItems(items);
        return response;
    }

    private GuildDetailResponse detail(GuildRecord guild, GuildMemberRecord selfMember) {
        GuildDetailResponse response = new GuildDetailResponse();
        response.setInGuild(true);
        response.setId(guild.getId());
        response.setName(guild.getName());
        response.setNotice(guild.getNotice());
        response.setLeaderCharacterId(guild.getLeaderCharacterId());
        response.setLeaderNickname(guild.getLeaderNickname());
        response.setMemberCount(guild.getMemberCount());
        response.setTotalContribution(guild.getTotalContribution());
        response.setCreatedAt(guild.getCreatedAt());
        String myRole = selfMember == null ? null : selfMember.getRole();
        response.setMyRole(myRole);
        response.setMyRoleText("leader".equals(myRole) ? "会长" : "成员");
        response.setMyContribution(selfMember == null ? 0 : selfMember.getContribution());
        response.setDonationOptions(donationResponses(selfMember));
        response.setShopItems(shopResponses(selfMember, null));
        response.setActivities(activityResponses(guild, selfMember == null ? 0 : selfMember.getCharacterId()));
        List<GuildMemberResponse> members = new ArrayList<GuildMemberResponse>();
        for (GuildMemberRecord member : guildRepository.listMembers(guild.getId())) {
            members.add(GuildMemberResponse.from(member));
        }
        response.setMembers(members);
        return response;
    }

    private PlayerCharacter requireCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        return character;
    }

    private GuildRecord requireGuild(long guildId) {
        GuildRecord guild = guildRepository.findById(guildId);
        if (guild == null) {
            throw new BusinessException("GUILD_NOT_FOUND", "公会不存在");
        }
        return guild;
    }

    private GuildMemberRecord requireMember(long characterId) {
        GuildMemberRecord member = guildRepository.findMemberByCharacterId(characterId);
        if (member == null) {
            throw new BusinessException("GUILD_NOT_JOINED", "当前角色尚未加入公会");
        }
        return member;
    }

    private void requireLeader(GuildMemberRecord member) {
        if (!"leader".equals(member.getRole())) {
            throw new BusinessException("GUILD_LEADER_REQUIRED", "只有会长可以执行该操作");
        }
    }

    private GuildDonationOptionConfig requireDonationOption(String donationId) {
        for (GuildDonationOptionConfig option : donationConfigs()) {
            if (option.getId().equals(donationId)) {
                return option;
            }
        }
        throw new BusinessException("GUILD_DONATION_NOT_FOUND", "捐献选项不存在");
    }

    private GuildShopItemConfig requireShopItem(String shopItemId) {
        for (GuildShopItemConfig shopItem : shopItemConfigs()) {
            if (shopItem.getId().equals(shopItemId)) {
                return shopItem;
            }
        }
        throw new BusinessException("GUILD_SHOP_ITEM_NOT_FOUND", "公会商店商品不存在");
    }

    private GuildActivityConfig requireActivity(String activityId) {
        for (GuildActivityConfig activity : activityConfigs()) {
            if (activity.getId().equals(activityId)) {
                return activity;
            }
        }
        throw new BusinessException("GUILD_ACTIVITY_NOT_FOUND", "公会活动不存在");
    }

    private List<GuildDonationOptionResponse> donationResponses(GuildMemberRecord member) {
        if (member == null) {
            return Collections.emptyList();
        }
        List<GuildDonationOptionResponse> result = new ArrayList<GuildDonationOptionResponse>();
        for (GuildDonationOptionConfig option : donationConfigs()) {
            GuildDonationOptionResponse response = new GuildDonationOptionResponse();
            response.setId(option.getId());
            response.setName(option.getName());
            response.setDescription(option.getDescription());
            response.setGoldCost(option.getGoldCost());
            response.setContribution(option.getContribution());
            response.setDailyLimit(option.getDailyLimit());
            int used = guildRepository.donationCountToday(member.getCharacterId(), option.getId());
            response.setDailyUsed(used);
            response.setRemainingTimes(Math.max(0, option.getDailyLimit() - used));
            result.add(response);
        }
        return result;
    }

    private List<GuildShopItemResponse> shopResponses(GuildMemberRecord member, Integer currentGold) {
        if (member == null) {
            return Collections.emptyList();
        }
        int gold = currentGold == null ? 0 : currentGold.intValue();
        if (currentGold == null) {
            PlayerCharacter character = characterRepository.findById(member.getCharacterId());
            gold = character == null ? 0 : character.getGold();
        }
        List<GuildShopItemResponse> result = new ArrayList<GuildShopItemResponse>();
        List<GuildShopItemConfig> configs = new ArrayList<GuildShopItemConfig>(shopItemConfigs());
        Collections.sort(configs, Comparator.comparingInt(GuildShopItemConfig::getSortOrder));
        for (GuildShopItemConfig config : configs) {
            ItemConfig item = gameConfigService.getItemRequired(config.getItemId());
            int used = guildRepository.purchaseCountToday(member.getCharacterId(), config.getId());
            GuildShopItemResponse response = new GuildShopItemResponse();
            response.setId(config.getId());
            response.setItemId(item.getId());
            response.setItemName(item.getName());
            response.setItemType(item.getType());
            response.setQuantity(config.getQuantity());
            response.setContributionCost(config.getContributionCost());
            response.setGoldCost(config.getGoldCost());
            response.setDailyLimit(config.getDailyLimit());
            response.setDailyUsed(used);
            response.setRemainingTimes(Math.max(0, config.getDailyLimit() - used));
            response.setMinContribution(config.getMinContribution());
            response.setTag(config.getTag());
            response.setDescription(config.getDescription());
            response.setCanBuy(response.getRemainingTimes() > 0
                    && member.getContribution() >= config.getMinContribution()
                    && member.getContribution() >= config.getContributionCost()
                    && gold >= config.getGoldCost());
            result.add(response);
        }
        return result;
    }

    private List<GuildActivityResponse> activityResponses(GuildRecord guild, long characterId) {
        if (guild == null || characterId <= 0) {
            return Collections.emptyList();
        }
        List<GuildActivityConfig> configs = new ArrayList<GuildActivityConfig>(activityConfigs());
        Collections.sort(configs, Comparator.comparingInt(GuildActivityConfig::getSortOrder));
        List<GuildActivityResponse> result = new ArrayList<GuildActivityResponse>();
        for (GuildActivityConfig config : configs) {
            boolean achieved = guild.getTotalContribution() >= config.getTargetContribution();
            boolean claimed = guildRepository.hasClaimedActivity(guild.getId(), characterId, config.getId());
            GuildActivityResponse response = new GuildActivityResponse();
            response.setId(config.getId());
            response.setName(config.getName());
            response.setDescription(config.getDescription());
            response.setTag(config.getTag());
            response.setTargetContribution(config.getTargetContribution());
            response.setCurrentContribution(guild.getTotalContribution());
            response.setProgressPercent(config.getTargetContribution() <= 0
                    ? 0
                    : Math.min(100, guild.getTotalContribution() * 100 / config.getTargetContribution()));
            response.setRewardGold(config.getRewardGold());
            response.setRewardItems(rewardItemResponses(config));
            response.setAchieved(achieved);
            response.setClaimed(claimed);
            response.setClaimable(achieved && !claimed);
            result.add(response);
        }
        return result;
    }

    private List<GuildDonationOptionConfig> donationConfigs() {
        GuildConfig config = gameConfigService.getGuildConfig();
        return config.getDonations() == null
                ? Collections.<GuildDonationOptionConfig>emptyList()
                : config.getDonations();
    }

    private List<GuildShopItemConfig> shopItemConfigs() {
        GuildConfig config = gameConfigService.getGuildConfig();
        return config.getShopItems() == null
                ? Collections.<GuildShopItemConfig>emptyList()
                : config.getShopItems();
    }

    private List<GuildActivityConfig> activityConfigs() {
        GuildConfig config = gameConfigService.getGuildConfig();
        return config.getActivities() == null
                ? Collections.<GuildActivityConfig>emptyList()
                : config.getActivities();
    }

    private List<InventoryItemGrant> rewardGrants(GuildActivityConfig activity) {
        List<InventoryItemGrant> grants = new ArrayList<InventoryItemGrant>();
        if (activity.getRewardItems() != null) {
            for (TaskRewardItemConfig rewardItem : activity.getRewardItems()) {
                grants.add(new InventoryItemGrant(rewardItem.getItemId(), rewardItem.getQuantity()));
            }
        }
        return grants;
    }

    private List<TaskRewardItemResponse> rewardItemResponses(GuildActivityConfig activity) {
        List<TaskRewardItemResponse> responses = new ArrayList<TaskRewardItemResponse>();
        if (activity.getRewardItems() != null) {
            for (TaskRewardItemConfig rewardItem : activity.getRewardItems()) {
                ItemConfig item = gameConfigService.getItemRequired(rewardItem.getItemId());
                responses.add(new TaskRewardItemResponse(item.getId(), item.getName(), rewardItem.getQuantity()));
            }
        }
        return responses;
    }

    private String rewardItemsJson(List<TaskRewardItemResponse> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            TaskRewardItemResponse item = items.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append("{\"itemId\":\"").append(escapeJson(item.getItemId()))
                    .append("\",\"name\":\"").append(escapeJson(item.getName()))
                    .append("\",\"quantity\":").append(item.getQuantity()).append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private String normalizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.length() < NAME_MIN_LENGTH || value.length() > NAME_MAX_LENGTH) {
            throw new BusinessException("GUILD_NAME_INVALID", "公会名称需为 2-12 个字符");
        }
        return value;
    }

    private String normalizeNotice(String notice) {
        String value = notice == null ? "" : notice.trim();
        if (value.length() > NOTICE_MAX_LENGTH) {
            throw new BusinessException("GUILD_NOTICE_TOO_LONG", "公会公告不能超过 80 个字符");
        }
        return value;
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
