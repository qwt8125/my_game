package com.paly.legend.common;

public class CurrentUser {

    private final long accountId;
    private final String username;

    public CurrentUser(long accountId, String username) {
        this.accountId = accountId;
        this.username = username;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }
}

