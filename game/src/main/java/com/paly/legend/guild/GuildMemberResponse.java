package com.paly.legend.guild;

public class GuildMemberResponse {

    private long characterId;
    private String nickname;
    private int level;
    private int power;
    private String role;
    private String roleText;
    private int contribution;
    private String joinedAt;

    public static GuildMemberResponse from(GuildMemberRecord record) {
        GuildMemberResponse response = new GuildMemberResponse();
        response.setCharacterId(record.getCharacterId());
        response.setNickname(record.getNickname());
        response.setLevel(record.getLevel());
        response.setPower(record.getPower());
        response.setRole(record.getRole());
        response.setRoleText("leader".equals(record.getRole()) ? "会长" : "成员");
        response.setContribution(record.getContribution());
        response.setJoinedAt(record.getJoinedAt());
        return response;
    }

    public long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleText() {
        return roleText;
    }

    public void setRoleText(String roleText) {
        this.roleText = roleText;
    }

    public int getContribution() {
        return contribution;
    }

    public void setContribution(int contribution) {
        this.contribution = contribution;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }
}
