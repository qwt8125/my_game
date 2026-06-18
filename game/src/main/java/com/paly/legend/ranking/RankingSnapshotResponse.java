package com.paly.legend.ranking;

import java.util.List;

public class RankingSnapshotResponse {

    private String type;
    private String title;
    private int limit;
    private String source;
    private String generatedAt;
    private String nextRefreshAt;
    private long refreshIntervalSeconds;
    private long secondsUntilRefresh;
    private List<RankingEntryResponse> entries;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getNextRefreshAt() {
        return nextRefreshAt;
    }

    public void setNextRefreshAt(String nextRefreshAt) {
        this.nextRefreshAt = nextRefreshAt;
    }

    public long getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }

    public void setRefreshIntervalSeconds(long refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    public long getSecondsUntilRefresh() {
        return secondsUntilRefresh;
    }

    public void setSecondsUntilRefresh(long secondsUntilRefresh) {
        this.secondsUntilRefresh = secondsUntilRefresh;
    }

    public List<RankingEntryResponse> getEntries() {
        return entries;
    }

    public void setEntries(List<RankingEntryResponse> entries) {
        this.entries = entries;
    }
}
