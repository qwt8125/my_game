package com.paly.legend.auth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.paly.legend.character.CharacterRepository;
import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final AccountRepository accountRepository;
    private final CharacterRepository characterRepository;
    private final PasswordEncoder passwordEncoder;
    private final int tokenHours;

    public AuthService(
            AccountRepository accountRepository,
            CharacterRepository characterRepository,
            PasswordEncoder passwordEncoder,
            @Value("${game.auth.token-hours:24}") int tokenHours) {
        this.accountRepository = accountRepository;
        this.characterRepository = characterRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenHours = tokenHours;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (accountRepository.usernameExists(username)) {
            throw new BusinessException("AUTH_USERNAME_EXISTS", "用户名已存在");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        Long accountId = accountRepository.createAccount(username, passwordHash);
        return new RegisterResponse(accountId);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        String username = normalizeUsername(request.getUsername());
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new BusinessException("AUTH_INVALID_CREDENTIALS", "账号或密码错误", HttpStatus.UNAUTHORIZED);
        }
        if (account.getStatus() != 0) {
            accountRepository.writeLoginLog(account.getId(), clientIp(servletRequest), servletRequest.getHeader("User-Agent"), false, "ACCOUNT_DISABLED");
            throw new BusinessException("AUTH_ACCOUNT_DISABLED", "账号已被禁用", HttpStatus.FORBIDDEN);
        }
        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            accountRepository.writeLoginLog(account.getId(), clientIp(servletRequest), servletRequest.getHeader("User-Agent"), false, "INVALID_PASSWORD");
            throw new BusinessException("AUTH_INVALID_CREDENTIALS", "账号或密码错误", HttpStatus.UNAUTHORIZED);
        }

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenHours);
        accountRepository.saveToken(account.getId(), token, expiresAt);
        accountRepository.writeLoginLog(account.getId(), clientIp(servletRequest), servletRequest.getHeader("User-Agent"), true, null);
        return new LoginResponse(token, FORMATTER.format(expiresAt));
    }

    public MeResponse me(CurrentUser currentUser) {
        boolean characterCreated = characterRepository.existsByAccountId(currentUser.getAccountId());
        return new MeResponse(currentUser.getAccountId(), currentUser.getUsername(), characterCreated);
    }

    @Transactional
    public void changePassword(CurrentUser currentUser, ChangePasswordRequest request) {
        Account account = accountRepository.findById(currentUser.getAccountId());
        if (account == null || account.getStatus() != 0) {
            throw new BusinessException("AUTH_TOKEN_INVALID", "登录已失效，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new BusinessException("AUTH_OLD_PASSWORD_INVALID", "原密码不正确", HttpStatus.BAD_REQUEST);
        }
        if (passwordEncoder.matches(request.getNewPassword(), account.getPasswordHash())) {
            throw new BusinessException("AUTH_PASSWORD_UNCHANGED", "新密码不能与原密码相同", HttpStatus.BAD_REQUEST);
        }
        accountRepository.updatePasswordHash(account.getId(), passwordEncoder.encode(request.getNewPassword()));
    }

    public CurrentUser validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("AUTH_REQUIRED", "请先登录", HttpStatus.UNAUTHORIZED);
        }

        TokenRecord record = accountRepository.findToken(token.trim());
        if (record == null || record.isRevoked() || record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("AUTH_TOKEN_INVALID", "登录已失效，请重新登录", HttpStatus.UNAUTHORIZED);
        }

        Account account = accountRepository.findById(record.getAccountId());
        if (account == null || account.getStatus() != 0) {
            throw new BusinessException("AUTH_TOKEN_INVALID", "登录已失效，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        return new CurrentUser(account.getId(), account.getUsername());
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            int comma = forwardedFor.indexOf(',');
            return comma > 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        return request.getRemoteAddr();
    }
}
