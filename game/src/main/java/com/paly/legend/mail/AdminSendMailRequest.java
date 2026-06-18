package com.paly.legend.mail;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AdminSendMailRequest {

    private long characterId;

    private List<Long> characterIds;

    private boolean all;

    @NotBlank
    @Size(max = 80)
    private String title;

    @Size(max = 500)
    private String content;

    private int gold;

    private String itemId;

    private int quantity;

    private String expiresAt;

    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public List<Long> getCharacterIds() { return characterIds; }
    public void setCharacterIds(List<Long> characterIds) { this.characterIds = characterIds; }
    public boolean isAll() { return all; }
    public void setAll(boolean all) { this.all = all; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
