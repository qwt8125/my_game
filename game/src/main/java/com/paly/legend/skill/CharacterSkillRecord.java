package com.paly.legend.skill;

public class CharacterSkillRecord {

    private long id;
    private long characterId;
    private String skillId;
    private int level;
    private int skillSlot;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getCharacterId() { return characterId; }
    public void setCharacterId(long characterId) { this.characterId = characterId; }
    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getSkillSlot() { return skillSlot; }
    public void setSkillSlot(int skillSlot) { this.skillSlot = skillSlot; }
}
