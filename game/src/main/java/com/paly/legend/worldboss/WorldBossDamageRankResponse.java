package com.paly.legend.worldboss;

public class WorldBossDamageRankResponse {

    private int rank;
    private long characterId;
    private String nickname;
    private int damage;
    private boolean rewarded;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public boolean isRewarded() { return rewarded; }
    public void setRewarded(boolean rewarded) { this.rewarded = rewarded; }
}
