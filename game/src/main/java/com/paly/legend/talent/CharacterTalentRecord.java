package com.paly.legend.talent;

public class CharacterTalentRecord {

    private long id;
    private long characterId;
    private String talentId;
    private int level;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public String getTalentId() { return talentId; }
    public void setTalentId(String talentId) { this.talentId = talentId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
