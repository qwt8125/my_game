package com.paly.legend.mail;

public class MailRecord {

    private long id;
    private long characterId;
    private String title;
    private String content;
    private int attachmentGold;
    private String attachmentItemId;
    private String attachmentItemType;
    private int attachmentQuantity;
    private int status;
    private String readAt;
    private boolean deleted;
    private String expiresAt;
    private String sourceType;
    private String sourceId;
    private String createdAt;
    private String claimedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getAttachmentGold() { return attachmentGold; }
    public void setAttachmentGold(int attachmentGold) { this.attachmentGold = attachmentGold; }
    public String getAttachmentItemId() { return attachmentItemId; }
    public void setAttachmentItemId(String attachmentItemId) { this.attachmentItemId = attachmentItemId; }
    public String getAttachmentItemType() { return attachmentItemType; }
    public void setAttachmentItemType(String attachmentItemType) { this.attachmentItemType = attachmentItemType; }
    public int getAttachmentQuantity() { return attachmentQuantity; }
    public void setAttachmentQuantity(int attachmentQuantity) { this.attachmentQuantity = attachmentQuantity; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getReadAt() { return readAt; }
    public void setReadAt(String readAt) { this.readAt = readAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getClaimedAt() { return claimedAt; }
    public void setClaimedAt(String claimedAt) { this.claimedAt = claimedAt; }
}
