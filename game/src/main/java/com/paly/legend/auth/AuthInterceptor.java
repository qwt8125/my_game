package com.paly.legend.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.paly.legend.common.AuthContext;
import com.paly.legend.common.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final OnlineStatusService onlineStatusService;

    public AuthInterceptor(AuthService authService, OnlineStatusService onlineStatusService) {
        this.authService = authService;
        this.onlineStatusService = onlineStatusService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("AUTH_REQUIRED", "请先登录", HttpStatus.UNAUTHORIZED);
        }

        String token = authorization.substring("Bearer ".length()).trim();
        com.paly.legend.common.CurrentUser currentUser = authService.validateToken(token);
        AuthContext.set(currentUser);
        onlineStatusService.markActive(currentUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
