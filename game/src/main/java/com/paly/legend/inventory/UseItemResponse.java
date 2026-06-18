package com.paly.legend.inventory;

public class UseItemResponse {

    private long inventoryItemId;
    private String itemId;
    private String name;
    private int remainingQuantity;
    private int bonusHp;
    private int bonusAttack;
    private int bonusDefense;
    private int bonusAttackSpeed;

    public long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(long inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public int getBonusHp() { return bonusHp; }
    public void setBonusHp(int bonusHp) { this.bonusHp = bonusHp; }
    public int getBonusAttack() { return bonusAttack; }
    public void setBonusAttack(int bonusAttack) { this.bonusAttack = bonusAttack; }
    public int getBonusDefense() { return bonusDefense; }
    public void setBonusDefense(int bonusDefense) { this.bonusDefense = bonusDefense; }
    public int getBonusAttackSpeed() { return bonusAttackSpeed; }
    public void setBonusAttackSpeed(int bonusAttackSpeed) { this.bonusAttackSpeed = bonusAttackSpeed; }
}
