package com.paly.legend.ranking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RankingRepository rankingRepository;
    private final RankingCacheService rankingCacheService;

    public RankingService(RankingRepository rankingRepository, RankingCacheService rankingCacheService) {
        this.rankingRepository = rankingRepository;
        this.rankingCacheService = rankingCacheService;
    }

    public List<RankingEntryResponse> level(int limit) {
        return list("level", normalizeLimit(limit));
    }

    public List<RankingEntryResponse> power(int limit) {
        return list("power", normalizeLimit(limit));
    }

    public List<RankingEntryResponse> gold(int limit) {
        return list("gold", normalizeLimit(limit));
    }

    public RankingSnapshotResponse levelSnapshot(int limit) {
        return snapshot("level", "等级榜", normalizeLimit(limit));
    }

    public RankingSnapshotResponse powerSnapshot(int limit) {
        return snapshot("power", "战力榜", normalizeLimit(limit));
    }

    public RankingSnapshotResponse goldSnapshot(int limit) {
        return snapshot("gold", "财富榜", normalizeLimit(limit));
    }

    public int currentRank(String type, long characterId) {
        String normalized = normalizeType(type);
        return rankingRepository.rankOf(normalized, characterId);
    }

    private List<RankingEntryResponse> list(String type, int limit) {
        List<RankingEntryResponse> cached = rankingCacheService.get(type, limit);
        if (cached != null) {
            return cached;
        }
        List<RankingEntryResponse> entries = rankingRepository.list(type, limit);
        rankingCacheService.put(type, limit, entries);
        return entries;
    }

    private RankingSnapshotResponse snapshot(String type, String title, int limit) {
        RankingSnapshotResponse cached = rankingCacheService.getSnapshot(type, limit);
        if (cached != null) {
            return cached;
        }

        List<RankingEntryResponse> entries = rankingRepository.list(type, limit);
        LocalDateTime now = LocalDateTime.now();
        long ttlSeconds = rankingCacheService.ttlSeconds();
        RankingSnapshotResponse snapshot = new RankingSnapshotResponse();
        snapshot.setType(type);
        snapshot.setTitle(title);
        snapshot.setLimit(limit);
        snapshot.setSource("database");
        snapshot.setGeneratedAt(FORMATTER.format(now));
        snapshot.setNextRefreshAt(FORMATTER.format(now.plusSeconds(ttlSeconds)));
        snapshot.setRefreshIntervalSeconds(ttlSeconds);
        snapshot.setSecondsUntilRefresh(ttlSeconds);
        snapshot.setEntries(entries);
        rankingCacheService.put(type, limit, entries);
        rankingCacheService.putSnapshot(type, limit, snapshot);
        return snapshot;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, 100);
    }

    private String normalizeType(String type) {
        if ("level".equals(type) || "power".equals(type) || "gold".equals(type)) {
            return type;
        }
        return "level";
    }
}
