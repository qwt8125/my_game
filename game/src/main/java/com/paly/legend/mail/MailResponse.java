package com.paly.legend.mail;

import com.paly.legend.config.ItemConfig;

public class MailResponse {

    private long id;
    private String title;
    private String content;
    private int attachmentGold;
    private String attachmentItemId;
    private String attachmentItemName;
    private int attachmentQuantity;
    private int status;
    private boolean claimable;
    private boolean read;
    private boolean deleted;
    private boolean expired;
    private String expiresAt;
    private String sourceType;
    private String createdAt;
    private String claimedAt;

    public static MailResponse from(MailRecord record, ItemConfig item) {
        MailResponse response = new MailResponse();
        response.setId(record.getId());
        response.setTitle(record.getTitle());
        response.setContent(record.getContent());
        response.setAttachmentGold(record.getAttachmentGold());
        response.setAttachmentItemId(record.getAttachmentItemId());
        response.setAttachmentItemName(item == null ? null : item.getName());
        response.setAttachmentQuantity(record.getAttachmentQuantity());
        response.setStatus(record.getStatus());
        response.setRead(record.getReadAt() != null && !record.getReadAt().trim().isEmpty());
        response.setDeleted(record.isDeleted());
        response.setExpiresAt(record.getExpiresAt());
        response.setExpired(isExpired(record.getExpiresAt()));
        response.setClaimable(record.getStatus() == 0
                && !response.isExpired()
                && (record.getAttachmentGold() > 0 || record.getAttachmentQuantity() > 0));
        response.setSourceType(record.getSourceType());
        response.setCreatedAt(record.getCreatedAt());
        response.setClaimedAt(record.getClaimedAt());
        return response;
    }

    private static boolean isExpired(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            return false;
        }
        try {
            java.time.LocalDateTime value = java.time.LocalDateTime.parse(expiresAt.replace(" ", "T"));
            return value.isBefore(java.time.LocalDateTime.now());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getAttachmentGold() { return attachmentGold; }
    public void setAttachmentGold(int attachmentGold) { this.attachmentGold = attachmentGold; }
    public String getAttachmentItemId() { return attachmentItemId; }
    public void setAttachmentItemId(String attachmentItemId) { this.attachmentItemId = attachmentItemId; }
    public String getAttachmentItemName() { return attachmentItemName; }
    public void setAttachmentItemName(String attachmentItemName) { this.attachmentItemName = attachmentItemName; }
    public int getAttachmentQuantity() { return attachmentQuantity; }
    public void setAttachmentQuantity(int attachmentQuantity) { this.attachmentQuantity = attachmentQuantity; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public boolean isClaimable() { return claimable; }
    public void setClaimable(boolean claimable) { this.claimable = claimable; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getClaimedAt() { return claimedAt; }
    public void setClaimedAt(String claimedAt) { this.claimedAt = claimedAt; }
}
