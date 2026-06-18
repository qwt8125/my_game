package com.paly.legend.guild;

public class GuildSummaryResponse {

    private long id;
    private String name;
    private String notice;
    private long leaderCharacterId;
    private String leaderNickname;
    private int memberCount;
    private int totalContribution;
    private String createdAt;

    public static GuildSummaryResponse from(GuildRecord record) {
        GuildSummaryResponse response = new GuildSummaryResponse();
        response.setId(record.getId());
        response.setName(record.getName());
        response.setNotice(record.getNotice());
        response.setLeaderCharacterId(record.getLeaderCharacterId());
        response.setLeaderNickname(record.getLeaderNickname());
        response.setMemberCount(record.getMemberCount());
        response.setTotalContribution(record.getTotalContribution());
        response.setCreatedAt(record.getCreatedAt());
        return response;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public long getLeaderCharacterId() {
        return leaderCharacterId;
    }

    public void setLeaderCharacterId(long leaderCharacterId) {
        this.leaderCharacterId = leaderCharacterId;
    }

    public String getLeaderNickname() {
        return leaderNickname;
    }

    public void setLeaderNickname(String leaderNickname) {
        this.leaderNickname = leaderNickname;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getTotalContribution() {
        return totalContribution;
    }

    public void setTotalContribution(int totalContribution) {
        this.totalContribution = totalContribution;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
