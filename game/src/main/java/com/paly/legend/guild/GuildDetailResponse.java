package com.paly.legend.guild;

import java.util.ArrayList;
import java.util.List;

public class GuildDetailResponse {

    private boolean inGuild;
    private long id;
    private String name;
    private String notice;
    private long leaderCharacterId;
    private String leaderNickname;
    private int memberCount;
    private int totalContribution;
    private int myContribution;
    private String createdAt;
    private String myRole;
    private String myRoleText;
    private List<GuildDonationOptionResponse> donationOptions = new ArrayList<GuildDonationOptionResponse>();
    private List<GuildShopItemResponse> shopItems = new ArrayList<GuildShopItemResponse>();
    private List<GuildActivityResponse> activities = new ArrayList<GuildActivityResponse>();
    private List<GuildMemberResponse> members = new ArrayList<GuildMemberResponse>();

    public boolean isInGuild() {
        return inGuild;
    }

    public void setInGuild(boolean inGuild) {
        this.inGuild = inGuild;
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

    public int getMyContribution() {
        return myContribution;
    }

    public void setMyContribution(int myContribution) {
        this.myContribution = myContribution;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public String getMyRoleText() {
        return myRoleText;
    }

    public void setMyRoleText(String myRoleText) {
        this.myRoleText = myRoleText;
    }

    public List<GuildDonationOptionResponse> getDonationOptions() {
        return donationOptions;
    }

    public void setDonationOptions(List<GuildDonationOptionResponse> donationOptions) {
        this.donationOptions = donationOptions;
    }

    public List<GuildShopItemResponse> getShopItems() {
        return shopItems;
    }

    public void setShopItems(List<GuildShopItemResponse> shopItems) {
        this.shopItems = shopItems;
    }

    public List<GuildActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<GuildActivityResponse> activities) {
        this.activities = activities;
    }

    public List<GuildMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<GuildMemberResponse> members) {
        this.members = members;
    }
}
