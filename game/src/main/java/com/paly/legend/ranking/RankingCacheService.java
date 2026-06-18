package com.paly.legend.ranking;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RankingCacheService {

    private static final String KEY_PREFIX = "legend:ranking:";
    private static final String SNAPSHOT_KEY_PREFIX = "legend:ranking:snapshot:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final TypeReference<List<RankingEntryResponse>> RANKING_LIST_TYPE =
            new TypeReference<List<RankingEntryResponse>>() {
            };
    private static final TypeReference<RankingSnapshotResponse> RANKING_SNAPSHOT_TYPE =
            new TypeReference<RankingSnapshotResponse>() {
            };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final long ttlSeconds;

    public RankingCacheService(StringRedisTemplate redisTemplate,
                               ObjectMapper objectMapper,
                               @Value("${game.ranking.cache-ttl-seconds:60}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttlSeconds = Math.max(5L, ttlSeconds);
    }

    public List<RankingEntryResponse> get(String type, int limit) {
        String key = key(type, limit);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, RANKING_LIST_TYPE);
        } catch (IOException | RuntimeException ex) {
            deleteQuietly(key);
            return null;
        }
    }

    public void put(String type, int limit, List<RankingEntryResponse> entries) {
        try {
            redisTemplate.opsForValue().set(key(type, limit), objectMapper.writeValueAsString(entries),
                    Duration.ofSeconds(ttlSeconds));
        } catch (IOException | RuntimeException ex) {
            // Ranking cache is an optimization only. SQLite remains the source of truth.
        }
    }

    public RankingSnapshotResponse getSnapshot(String type, int limit) {
        String key = snapshotKey(type, limit);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            RankingSnapshotResponse snapshot = objectMapper.readValue(json, RANKING_SNAPSHOT_TYPE);
            snapshot.setSource("cache");
            Long seconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (seconds != null && seconds > 0) {
                snapshot.setSecondsUntilRefresh(seconds);
                snapshot.setNextRefreshAt(FORMATTER.format(LocalDateTime.now().plusSeconds(seconds)));
            }
            return snapshot;
        } catch (IOException | RuntimeException ex) {
            deleteQuietly(key);
            return null;
        }
    }

    public void putSnapshot(String type, int limit, RankingSnapshotResponse snapshot) {
        try {
            redisTemplate.opsForValue().set(snapshotKey(type, limit), objectMapper.writeValueAsString(snapshot),
                    Duration.ofSeconds(ttlSeconds));
        } catch (IOException | RuntimeException ex) {
            // Ranking snapshot cache is optional. SQLite remains the source of truth.
        }
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }

    private String key(String type, int limit) {
        return KEY_PREFIX + type + ":" + limit;
    }

    private String snapshotKey(String type, int limit) {
        return SNAPSHOT_KEY_PREFIX + type + ":" + limit;
    }

    private void deleteQuietly(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            // Cache deletion failure must not break ranking reads.
        }
    }
}
