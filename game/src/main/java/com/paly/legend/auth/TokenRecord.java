package com.paly.legend.auth;

import java.time.LocalDateTime;

public class TokenRecord {

    private final long accountId;
    private final String token;
    private final LocalDateTime expiresAt;
    private final boolean revoked;

    public TokenRecord(long accountId, String token, LocalDateTime expiresAt, boolean revoked) {
        this.accountId = accountId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}

