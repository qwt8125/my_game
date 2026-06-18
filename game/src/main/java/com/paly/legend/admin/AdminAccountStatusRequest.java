package com.paly.legend.admin;

import javax.validation.constraints.Min;

public class AdminAccountStatusRequest {

    @Min(1)
    private long accountId;

    private boolean disabled;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
