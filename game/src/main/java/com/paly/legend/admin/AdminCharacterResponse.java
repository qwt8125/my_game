package com.paly.legend.admin;

import com.paly.legend.character.PlayerCharacter;

public class AdminCharacterResponse {

    private long id;
    private long accountId;
    private String nickname;
    private int level;
    private int exp;
    private int gold;
    private int power;
    private int accountStatus;
    private String accountStatusText;
    private boolean online;
    private String lastActiveAt;

    public static AdminCharacterResponse from(PlayerCharacter character) {
        AdminCharacterResponse response = new AdminCharacterResponse();
        response.setId(character.getId());
        response.setAccountId(character.getAccountId());
        response.setNickname(character.getNickname());
        response.setLevel(character.getLevel());
        response.setExp(character.getExp());
        response.setGold(character.getGold());
        response.setPower(character.getPower());
        return response;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }
    public int getAccountStatus() { return accountStatus; }
    public void setAccountStatus(int accountStatus) { this.accountStatus = accountStatus; }
    public String getAccountStatusText() { return accountStatusText; }
    public void setAccountStatusText(String accountStatusText) { this.accountStatusText = accountStatusText; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public String getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(String lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}
