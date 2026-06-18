package com.paly.legend.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.paly.legend.auth.Account;
import com.paly.legend.auth.AccountRepository;
import com.paly.legend.auth.OnlineStatusService;
import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.ActivityConfig;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MapEventConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import com.paly.legend.mail.AdminSendMailRequest;
import com.paly.legend.mail.MailService;
import com.paly.legend.map.MapEventStateRecord;
import com.paly.legend.map.MapEventStateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final AccountRepository accountRepository;
    private final CharacterRepository characterRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryCapacityService inventoryCapacityService;
    private final BattleRepository battleRepository;
    private final GameConfigService gameConfigService;
    private final MapEventStateRepository mapEventStateRepository;
    private final JdbcTemplate jdbcTemplate;
    private final OnlineStatusService onlineStatusService;
    private final MailService mailService;
    private final String adminUsernames;

    public AdminService(AccountRepository accountRepository,
                        CharacterRepository characterRepository,
                        InventoryRepository inventoryRepository,
                        InventoryGrantService inventoryGrantService,
                        InventoryCapacityService inventoryCapacityService,
                        BattleRepository battleRepository,
                        GameConfigService gameConfigService,
                        MapEventStateRepository mapEventStateRepository,
                        JdbcTemplate jdbcTemplate,
                        OnlineStatusService onlineStatusService,
                        MailService mailService,
                        @Value("${game.admin.usernames:admin}") String adminUsernames) {
        this.accountRepository = accountRepository;
        this.characterRepository = characterRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryCapacityService = inventoryCapacityService;
        this.battleRepository = battleRepository;
        this.gameConfigService = gameConfigService;
        this.mapEventStateRepository = mapEventStateRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.onlineStatusService = onlineStatusService;
        this.mailService = mailService;
        this.adminUsernames = adminUsernames;
    }

    public List<AdminCharacterResponse> listCharacters(CurrentUser currentUser, int limit) {
        requireAdmin(currentUser);
        List<AdminCharacterResponse> result = new ArrayList<AdminCharacterResponse>();
        for (PlayerCharacter character : characterRepository.listForAdmin(limit)) {
            AdminCharacterResponse response = AdminCharacterResponse.from(character);
            Account account = accountRepository.findById(character.getAccountId());
            int status = account == null ? 1 : account.getStatus();
            response.setAccountStatus(status);
            response.setAccountStatusText(status == 0 ? "正常" : "禁用");
            response.setOnline(onlineStatusService.isOnline(character.getAccountId()));
            response.setLastActiveAt(onlineStatusService.lastActiveAt(character.getAccountId()));
            result.add(response);
        }
        return result;
    }

    public List<ActivityConfig> listActivities(CurrentUser currentUser) {
        requireAdmin(currentUser);
        return gameConfigService.listActivities();
    }

    @Transactional
    public AdminConfigReloadResponse updateActivity(CurrentUser currentUser, String activityId, ActivityConfig request) {
        Account admin = requireAdmin(currentUser);
        if (activityId == null || activityId.trim().isEmpty()) {
            throw new BusinessException("ACTIVITY_ID_REQUIRED", "活动 ID 不能为空");
        }
        if (request == null) {
            throw new BusinessException("ACTIVITY_CONFIG_REQUIRED", "活动配置不能为空");
        }
        List<ActivityConfig> nextActivities = new ArrayList<ActivityConfig>(gameConfigService.listActivities());
        boolean found = false;
        for (int i = 0; i < nextActivities.size(); i++) {
            ActivityConfig current = nextActivities.get(i);
            if (activityId.equals(current.getId())) {
                request.setId(activityId);
                nextActivities.set(i, request);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new BusinessException("ACTIVITY_NOT_FOUND", "活动不存在");
        }
        try {
            String summary = gameConfigService.saveActivities(nextActivities);
            writeGmSystemLog(admin.getId(), "update_activity_config",
                    "{\"activityId\":\"" + safe(activityId) + "\",\"success\":true,\"summary\":\"" + safe(summary) + "\"}");
            return new AdminConfigReloadResponse(true, "活动配置已保存并生效", summary);
        } catch (RuntimeException ex) {
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            writeGmSystemLog(admin.getId(), "update_activity_config",
                    "{\"activityId\":\"" + safe(activityId) + "\",\"success\":false,\"error\":\"" + safe(message) + "\"}");
            return new AdminConfigReloadResponse(false, "活动配置保存失败，已保留旧配置", message);
        }
    }

    @Transactional
    public AdminActionResponse grantGold(CurrentUser currentUser, AdminGrantGoldRequest request) {
        Account admin = requireAdmin(currentUser);
        PlayerCharacter target = getTarget(request.getCharacterId());
        int beforeGold = target.getGold();
        int afterGold = beforeGold + request.getGold();
        characterRepository.addGold(target.getId(), request.getGold());
        battleRepository.createCurrencyLog(
                target.getId(),
                request.getGold(),
                beforeGold,
                afterGold,
                "gm_grant",
                String.valueOf(admin.getId()));
        writeGmLog(admin.getId(), target.getId(), "grant_gold", "{\"gold\":" + request.getGold() + "}");
        return new AdminActionResponse(true, "金币发放成功");
    }

    @Transactional
    public AdminActionResponse grantItem(CurrentUser currentUser, AdminGrantItemRequest request) {
        Account admin = requireAdmin(currentUser);
        PlayerCharacter target = getTarget(request.getCharacterId());
        ItemConfig item = gameConfigService.getItemRequired(request.getItemId());
        inventoryCapacityService.requireSpaceFor(target.getId(),
                Collections.singletonList(new InventoryItemGrant(item.getId(), request.getQuantity())));
        inventoryGrantService.addItem(target.getId(), item, request.getQuantity());
        inventoryRepository.createDropLog(target.getId(), "gm", String.valueOf(admin.getId()), item.getId(), request.getQuantity());
        writeGmLog(admin.getId(), target.getId(), "grant_item",
                "{\"itemId\":\"" + item.getId() + "\",\"quantity\":" + request.getQuantity() + "}");
        return new AdminActionResponse(true, "物品发放成功");
    }

    @Transactional
    public AdminActionResponse sendMail(CurrentUser currentUser, AdminSendMailRequest request) {
        Account admin = requireAdmin(currentUser);
        List<PlayerCharacter> targets = resolveMailTargets(request);
        int sent = 0;
        long firstMailId = 0;
        for (PlayerCharacter target : targets) {
            long mailId = mailService.createMail(
                    target.getId(),
                    request.getTitle(),
                    request.getContent(),
                    request.getGold(),
                    request.getItemId(),
                    request.getQuantity(),
                    "gm",
                    String.valueOf(admin.getId()),
                    request.getExpiresAt());
            if (firstMailId == 0) {
                firstMailId = mailId;
            }
            sent++;
            writeGmLog(admin.getId(), target.getId(), "send_mail",
                    "{\"mailId\":" + mailId + ",\"gold\":" + request.getGold()
                            + ",\"itemId\":\"" + safe(request.getItemId()) + "\",\"quantity\":" + request.getQuantity()
                            + ",\"expiresAt\":\"" + safe(request.getExpiresAt()) + "\"}");
        }
        String scope = request.isAll() ? "全服" : (sent > 1 ? "批量" : "单人");
        return new AdminActionResponse(true, scope + "邮件发送成功，共 " + sent + " 封，首封 ID " + firstMailId);
    }

    public List<AdminMapEventStateResponse> listMapEventStates(CurrentUser currentUser, long characterId) {
        requireAdmin(currentUser);
        PlayerCharacter target = getTarget(characterId);
        List<AdminMapEventStateResponse> result = new ArrayList<AdminMapEventStateResponse>();
        for (MapEventStateRecord state : mapEventStateRepository.findByCharacterId(target.getId())) {
            result.add(toMapEventStateResponse(state));
        }
        return result;
    }

    @Transactional
    public AdminActionResponse resetMapEventState(CurrentUser currentUser, AdminMapEventResetRequest request) {
        Account admin = requireAdmin(currentUser);
        PlayerCharacter target = getTarget(request.getCharacterId());
        MapEventConfig event = gameConfigService.getMapEventRequired(request.getEventId());
        int deleted = mapEventStateRepository.delete(target.getId(), event.getId());
        writeGmLog(admin.getId(), target.getId(), "reset_map_event_state",
                "{\"eventId\":\"" + safe(event.getId()) + "\",\"deleted\":" + deleted + "}");
        return new AdminActionResponse(true, "地图事件状态重置成功，删除 " + deleted + " 条");
    }

    @Transactional
    public AdminActionResponse resetAllMapEventStates(CurrentUser currentUser, AdminMapEventResetAllRequest request) {
        Account admin = requireAdmin(currentUser);
        PlayerCharacter target = getTarget(request.getCharacterId());
        int deleted = mapEventStateRepository.deleteByCharacterId(target.getId());
        writeGmLog(admin.getId(), target.getId(), "reset_all_map_event_states",
                "{\"deleted\":" + deleted + "}");
        return new AdminActionResponse(true, "地图事件状态全部重置成功，删除 " + deleted + " 条");
    }

    @Transactional
    public AdminActionResponse cleanupMapEventStates(CurrentUser currentUser, AdminMapEventCleanupRequest request) {
        Account admin = requireAdmin(currentUser);
        PlayerCharacter target = getTarget(request.getCharacterId());
        int keepDays = Math.max(1, Math.min(365, request.getKeepDays()));
        LocalDateTime cutoff = LocalDateTime.now().minusDays(keepDays);
        int deleted = mapEventStateRepository.cleanupRepeatableBefore(target.getId(), cutoff);
        writeGmLog(admin.getId(), target.getId(), "cleanup_map_event_states",
                "{\"keepDays\":" + keepDays + ",\"cutoff\":\"" + FORMATTER.format(cutoff)
                        + "\",\"deleted\":" + deleted + "}");
        return new AdminActionResponse(true, "地图事件过期状态清理完成，删除 " + deleted + " 条");
    }

    @Transactional
    public AdminActionResponse updateAccountStatus(CurrentUser currentUser, AdminAccountStatusRequest request) {
        Account admin = requireAdmin(currentUser);
        Account target = accountRepository.findById(request.getAccountId());
        if (target == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账号不存在");
        }
        if (target.getId() == admin.getId() && request.isDisabled()) {
            throw new BusinessException("ADMIN_DISABLE_SELF", "不能禁用当前管理员账号");
        }
        int status = request.isDisabled() ? 1 : 0;
        accountRepository.updateStatus(target.getId(), status);
        PlayerCharacter targetCharacter = characterRepository.findByAccountId(target.getId());
        String payload = "{\"accountId\":" + target.getId()
                + ",\"username\":\"" + safe(target.getUsername()) + "\",\"status\":" + status + "}";
        if (targetCharacter == null) {
            writeGmSystemLog(admin.getId(), "account_status", payload);
        } else {
            writeGmLog(admin.getId(), targetCharacter.getId(), "account_status", payload);
        }
        return new AdminActionResponse(true, status == 0 ? "账号已解禁" : "账号已禁用");
    }

    @Transactional
    public AdminConfigReloadResponse reloadConfig(CurrentUser currentUser) {
        Account admin = requireAdmin(currentUser);
        try {
            String summary = gameConfigService.reload();
            writeGmSystemLog(admin.getId(), "reload_config", "{\"success\":true,\"summary\":\"" + safe(summary) + "\"}");
            return new AdminConfigReloadResponse(true, "配置重载成功", summary);
        } catch (RuntimeException ex) {
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            writeGmSystemLog(admin.getId(), "reload_config", "{\"success\":false,\"error\":\"" + safe(message) + "\"}");
            return new AdminConfigReloadResponse(false, "配置重载失败，已保留旧配置", message);
        }
    }

    private List<PlayerCharacter> resolveMailTargets(AdminSendMailRequest request) {
        if (request.isAll()) {
            List<PlayerCharacter> all = characterRepository.listAll();
            if (all.isEmpty()) {
                throw new BusinessException("MAIL_TARGET_EMPTY", "当前没有可接收邮件的角色");
            }
            return all;
        }
        List<PlayerCharacter> targets = new ArrayList<PlayerCharacter>();
        if (request.getCharacterIds() != null && !request.getCharacterIds().isEmpty()) {
            for (Long characterId : request.getCharacterIds()) {
                if (characterId == null || characterId <= 0) {
                    continue;
                }
                targets.add(getTarget(characterId));
            }
        } else if (request.getCharacterId() > 0) {
            targets.add(getTarget(request.getCharacterId()));
        }
        if (targets.isEmpty()) {
            throw new BusinessException("MAIL_TARGET_REQUIRED", "请填写角色 ID、角色 ID 列表或选择全服邮件");
        }
        return targets;
    }

    private Account requireAdmin(CurrentUser currentUser) {
        Account account = accountRepository.findById(currentUser.getAccountId());
        if (account == null || !isAdminUsername(account.getUsername())) {
            throw new BusinessException("ADMIN_FORBIDDEN", "无管理员权限");
        }
        return account;
    }

    private boolean isAdminUsername(String username) {
        if (username == null || adminUsernames == null) {
            return false;
        }
        String[] values = adminUsernames.split(",");
        for (String value : values) {
            if (username.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private PlayerCharacter getTarget(long characterId) {
        PlayerCharacter character = characterRepository.findById(characterId);
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_FOUND", "角色不存在");
        }
        return character;
    }

    private void writeGmLog(long adminAccountId, long targetCharacterId, String action, String payloadJson) {
        jdbcTemplate.update(
                "INSERT INTO gm_logs(admin_account_id, target_character_id, action, payload_json) VALUES(?, ?, ?, ?)",
                adminAccountId,
                targetCharacterId,
                action,
                payloadJson);
    }

    private void writeGmSystemLog(long adminAccountId, String action, String payloadJson) {
        jdbcTemplate.update(
                "INSERT INTO gm_system_logs(admin_account_id, action, payload_json) VALUES(?, ?, ?)",
                adminAccountId,
                action,
                payloadJson);
    }

    private AdminMapEventStateResponse toMapEventStateResponse(MapEventStateRecord state) {
        MapEventConfig event = gameConfigService.getMapEventRequired(state.getEventId());
        MapConfig map = gameConfigService.getMapRequired(event.getMapId());
        AdminMapEventStateResponse response = new AdminMapEventStateResponse();
        response.setEventId(event.getId());
        response.setEventName(event.getName());
        response.setMapId(map.getId());
        response.setMapName(map.getName());
        response.setType(event.getType());
        response.setResetType(event.getResetType());
        response.setRepeatable(event.isRepeatable());
        response.setCooldownSeconds(event.getCooldownSeconds());
        response.setTriggerCount(state.getTriggerCount());
        response.setLastTriggeredAt(state.getLastTriggeredAt() == null ? null : FORMATTER.format(state.getLastTriggeredAt()));
        response.setNextAvailableAt(state.getNextAvailableAt() == null ? null : FORMATTER.format(state.getNextAvailableAt()));
        response.setCompleted(state.isCompleted());
        return response;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "");
    }
}
