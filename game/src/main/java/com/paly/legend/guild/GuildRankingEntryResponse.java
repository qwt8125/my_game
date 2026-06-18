package com.paly.legend.guild;

public class GuildRankingEntryResponse {

    private int rank;
    private long guildId;
    private String name;
    private String leaderNickname;
    private int memberCount;
    private int totalContribution;
    private boolean mine;

    public static GuildRankingEntryResponse from(int rank, GuildRecord record, Long myGuildId) {
        GuildRankingEntryResponse response = new GuildRankingEntryResponse();
        response.setRank(rank);
        response.setGuildId(record.getId());
        response.setName(record.getName());
        response.setLeaderNickname(record.getLeaderNickname());
        response.setMemberCount(record.getMemberCount());
        response.setTotalContribution(record.getTotalContribution());
        response.setMine(myGuildId != null && myGuildId.longValue() == record.getId());
        return response;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
}
