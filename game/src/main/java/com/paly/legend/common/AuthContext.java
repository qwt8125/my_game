package com.paly.legend.common;

public final class AuthContext {

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<CurrentUser>();

    private AuthContext() {
    }

    public static void set(CurrentUser currentUser) {
        CURRENT.set(currentUser);
    }

    public static CurrentUser getRequired() {
        CurrentUser currentUser = CURRENT.get();
        if (currentUser == null) {
            throw new BusinessException("AUTH_REQUIRED", "请先登录", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return currentUser;
    }

    public static void clear() {
        CURRENT.remove();
    }
}

