package com.paly.legend.auth;

public class RegisterResponse {

    private long accountId;

    public RegisterResponse() {
    }

    public RegisterResponse(long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
}

