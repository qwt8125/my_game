package com.paly.legend.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.paly.legend.battle.BattleRepository;
import com.paly.legend.character.CharacterRepository;
import com.paly.legend.character.PlayerCharacter;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.ItemConfig;
import com.paly.legend.inventory.InventoryCapacityService;
import com.paly.legend.inventory.InventoryGrantService;
import com.paly.legend.inventory.InventoryItemGrant;
import com.paly.legend.inventory.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MailRepository mailRepository;
    private final CharacterRepository characterRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryGrantService inventoryGrantService;
    private final InventoryCapacityService inventoryCapacityService;
    private final BattleRepository battleRepository;
    private final GameConfigService gameConfigService;

    public MailService(MailRepository mailRepository,
                       CharacterRepository characterRepository,
                       InventoryRepository inventoryRepository,
                       InventoryGrantService inventoryGrantService,
                       InventoryCapacityService inventoryCapacityService,
                       BattleRepository battleRepository,
                       GameConfigService gameConfigService) {
        this.mailRepository = mailRepository;
        this.characterRepository = characterRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryGrantService = inventoryGrantService;
        this.inventoryCapacityService = inventoryCapacityService;
        this.battleRepository = battleRepository;
        this.gameConfigService = gameConfigService;
    }

    public List<MailResponse> list(CurrentUser currentUser, int limit) {
        PlayerCharacter character = requireCharacter(currentUser);
        List<MailResponse> result = new ArrayList<MailResponse>();
        for (MailRecord record : mailRepository.listByCharacterId(character.getId(), limit)) {
            result.add(MailResponse.from(record, findItem(record.getAttachmentItemId())));
        }
        return result;
    }

    @Transactional
    public MailClaimResponse claim(CurrentUser currentUser, long mailId) {
        PlayerCharacter character = requireCharacter(currentUser);
        MailRecord mail = mailRepository.findByIdForCharacter(mailId, character.getId());
        if (mail == null) {
            throw new BusinessException("MAIL_NOT_FOUND", "邮件不存在", HttpStatus.NOT_FOUND);
        }
        if (mail.getStatus() != 0) {
            throw new BusinessException("MAIL_ALREADY_CLAIMED", "邮件奖励已领取");
        }
        if (isExpired(mail.getExpiresAt())) {
            throw new BusinessException("MAIL_EXPIRED", "邮件已过期");
        }
        if (mail.getAttachmentGold() <= 0 && mail.getAttachmentQuantity() <= 0) {
            throw new BusinessException("MAIL_NO_ATTACHMENT", "邮件没有可领取附件");
        }
        if (mail.getAttachmentQuantity() > 0) {
            inventoryCapacityService.requireSpaceFor(character.getId(),
                    Collections.singletonList(new InventoryItemGrant(mail.getAttachmentItemId(), mail.getAttachmentQuantity())));
        }

        MailClaimResponse response = new MailClaimResponse();
        int currentGold = character.getGold();
        if (mail.getAttachmentGold() > 0) {
            int afterGold = character.getGold() + mail.getAttachmentGold();
            characterRepository.addGold(character.getId(), mail.getAttachmentGold());
            battleRepository.createCurrencyLog(character.getId(), mail.getAttachmentGold(),
                    character.getGold(), afterGold, "mail_claim", String.valueOf(mail.getId()));
            currentGold = afterGold;
            response.setGoldGained(mail.getAttachmentGold());
        }
        if (mail.getAttachmentQuantity() > 0) {
            ItemConfig item = gameConfigService.getItemRequired(mail.getAttachmentItemId());
            inventoryGrantService.addItem(character.getId(), item, mail.getAttachmentQuantity());
            inventoryRepository.createDropLog(character.getId(), "mail", String.valueOf(mail.getId()), item.getId(), mail.getAttachmentQuantity());
            response.setItemId(item.getId());
            response.setItemName(item.getName());
            response.setQuantity(mail.getAttachmentQuantity());
        }
        mailRepository.markClaimed(mail.getId());
        response.setCurrentGold(currentGold);
        return response;
    }

    public void markRead(CurrentUser currentUser, long mailId) {
        PlayerCharacter character = requireCharacter(currentUser);
        MailRecord mail = mailRepository.findByIdForCharacter(mailId, character.getId());
        if (mail == null) {
            throw new BusinessException("MAIL_NOT_FOUND", "邮件不存在", HttpStatus.NOT_FOUND);
        }
        mailRepository.markRead(mail.getId());
    }

    public void delete(CurrentUser currentUser, long mailId) {
        PlayerCharacter character = requireCharacter(currentUser);
        MailRecord mail = mailRepository.findByIdForCharacter(mailId, character.getId());
        if (mail == null) {
            throw new BusinessException("MAIL_NOT_FOUND", "邮件不存在", HttpStatus.NOT_FOUND);
        }
        if (mail.getStatus() == 0 && !isExpired(mail.getExpiresAt())
                && (mail.getAttachmentGold() > 0 || mail.getAttachmentQuantity() > 0)) {
            throw new BusinessException("MAIL_ATTACHMENT_NOT_CLAIMED", "请先领取附件或等待邮件过期后再删除");
        }
        mailRepository.markDeleted(mail.getId());
    }

    public long createMail(long characterId, String title, String content, int gold,
                           String itemId, int quantity, String sourceType, String sourceId) {
        return createMail(characterId, title, content, gold, itemId, quantity, sourceType, sourceId, null);
    }

    public long createMail(long characterId, String title, String content, int gold,
                           String itemId, int quantity, String sourceType, String sourceId, String expiresAt) {
        PlayerCharacter target = characterRepository.findById(characterId);
        if (target == null) {
            throw new BusinessException("CHARACTER_NOT_FOUND", "角色不存在", HttpStatus.NOT_FOUND);
        }
        ItemConfig item = null;
        if (itemId != null && !itemId.trim().isEmpty()) {
            item = gameConfigService.getItemRequired(itemId.trim());
            if (quantity <= 0) {
                throw new BusinessException("MAIL_ITEM_QUANTITY_REQUIRED", "物品数量必须大于 0");
            }
        } else if (quantity > 0) {
            throw new BusinessException("MAIL_ITEM_REQUIRED", "请填写物品 ID");
        }
        int safeGold = Math.max(0, gold);
        if (safeGold == 0 && (item == null || quantity <= 0)) {
            throw new BusinessException("MAIL_ATTACHMENT_REQUIRED", "至少需要填写金币或物品附件");
        }
        String safeExpiresAt = normalizeExpiresAt(expiresAt);
        return mailRepository.create(
                target.getId(),
                title.trim(),
                content == null ? "" : content.trim(),
                safeGold,
                item == null ? null : item.getId(),
                item == null ? null : item.getType(),
                item == null ? 0 : quantity,
                sourceType,
                sourceId,
                safeExpiresAt);
    }

    private PlayerCharacter requireCharacter(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_REQUIRED", "请先创建角色");
        }
        return character;
    }

    private ItemConfig findItem(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return null;
        }
        return gameConfigService.getItemRequired(itemId);
    }

    private String normalizeExpiresAt(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            return null;
        }
        String value = expiresAt.trim().replace("T", " ");
        if (value.length() == 16) {
            value = value + ":00";
        }
        try {
            LocalDateTime parsed = LocalDateTime.parse(value, FORMATTER);
            if (!parsed.isAfter(LocalDateTime.now())) {
                throw new BusinessException("MAIL_EXPIRES_AT_INVALID", "过期时间必须晚于当前时间");
            }
            return FORMATTER.format(parsed);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessException("MAIL_EXPIRES_AT_INVALID", "过期时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private boolean isExpired(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            return false;
        }
        try {
            String value = expiresAt.trim().replace("T", " ");
            if (value.length() == 16) {
                value = value + ":00";
            }
            return LocalDateTime.parse(value, FORMATTER).isBefore(LocalDateTime.now());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
