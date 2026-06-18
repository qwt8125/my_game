package com.paly.legend.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.paly.legend.common.CurrentUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OnlineStatusService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String ONLINE_KEY_PREFIX = "legend:online:account:";
    private static final Duration ONLINE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public OnlineStatusService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markActive(CurrentUser currentUser) {
        String value = currentUser.getUsername() + "|" + FORMATTER.format(LocalDateTime.now());
        try {
            redisTemplate.opsForValue().set(key(currentUser.getAccountId()), value, ONLINE_TTL);
        } catch (RuntimeException ex) {
            // Online status is a transient cache. Login and gameplay must continue if Redis is unavailable.
        }
    }

    public boolean isOnline(long accountId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key(accountId)));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String lastActiveAt(long accountId) {
        String value;
        try {
            value = redisTemplate.opsForValue().get(key(accountId));
        } catch (RuntimeException ex) {
            return null;
        }
        if (value == null) {
            return null;
        }
        int separator = value.lastIndexOf('|');
        return separator >= 0 ? value.substring(separator + 1) : null;
    }

    private String key(long accountId) {
        return ONLINE_KEY_PREFIX + accountId;
    }
}
