package com.paly.legend.auth;

public class MeResponse {

    private long accountId;
    private String username;
    private boolean characterCreated;

    public MeResponse() {
    }

    public MeResponse(long accountId, String username, boolean characterCreated) {
        this.accountId = accountId;
        this.username = username;
        this.characterCreated = characterCreated;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isCharacterCreated() {
        return characterCreated;
    }

    public void setCharacterCreated(boolean characterCreated) {
        this.characterCreated = characterCreated;
    }
}

