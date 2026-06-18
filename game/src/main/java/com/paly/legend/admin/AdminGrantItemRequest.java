package com.paly.legend.admin;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class AdminGrantItemRequest {

    @Min(1)
    private long characterId;

    @NotBlank
    private String itemId;

    @Min(1)
    private int quantity;

    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
