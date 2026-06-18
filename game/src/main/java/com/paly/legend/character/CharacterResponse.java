package com.paly.legend.character;

public class CharacterResponse {

    private long id;
    private String nickname;
    private String className;
    private String classDisplayName;
    private String classDescription;
    private int level;
    private int exp;
    private int gold;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;
    private int power;
    private String currentMapId;
    private String currentNodeId;
    private int lastX;
    private int lastY;
    private int talentPoints;
    private int usedTalentPoints;
    private int availableTalentPoints;

    public static CharacterResponse from(PlayerCharacter character) {
        CharacterResponse response = new CharacterResponse();
        response.setId(character.getId());
        response.setNickname(character.getNickname());
        response.setClassName(character.getClassName());
        response.setLevel(character.getLevel());
        response.setExp(character.getExp());
        response.setGold(character.getGold());
        response.setHp(character.getHp());
        response.setAttack(character.getAttack());
        response.setDefense(character.getDefense());
        response.setAttackSpeed(character.getAttackSpeed());
        response.setPower(character.getPower());
        response.setCurrentMapId(character.getCurrentMapId());
        response.setCurrentNodeId(character.getCurrentNodeId());
        response.setLastX(character.getLastX());
        response.setLastY(character.getLastY());
        return response;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassDisplayName() {
        return classDisplayName;
    }

    public void setClassDisplayName(String classDisplayName) {
        this.classDisplayName = classDisplayName;
    }

    public String getClassDescription() {
        return classDescription;
    }

    public void setClassDescription(String classDescription) {
        this.classDescription = classDescription;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getCurrentMapId() {
        return currentMapId;
    }

    public void setCurrentMapId(String currentMapId) {
        this.currentMapId = currentMapId;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public int getLastX() {
        return lastX;
    }

    public void setLastX(int lastX) {
        this.lastX = lastX;
    }

    public int getLastY() {
        return lastY;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    public int getTalentPoints() {
        return talentPoints;
    }

    public void setTalentPoints(int talentPoints) {
        this.talentPoints = talentPoints;
    }

    public int getUsedTalentPoints() {
        return usedTalentPoints;
    }

    public void setUsedTalentPoints(int usedTalentPoints) {
        this.usedTalentPoints = usedTalentPoints;
    }

    public int getAvailableTalentPoints() {
        return availableTalentPoints;
    }

    public void setAvailableTalentPoints(int availableTalentPoints) {
        this.availableTalentPoints = availableTalentPoints;
    }
}
