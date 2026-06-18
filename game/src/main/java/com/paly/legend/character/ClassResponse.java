package com.paly.legend.character;

import com.paly.legend.config.ClassConfig;

public class ClassResponse {

    private String id;
    private String name;
    private String description;
    private int hp;
    private int attack;
    private int defense;
    private int attackSpeed;

    public static ClassResponse from(ClassConfig config) {
        ClassResponse response = new ClassResponse();
        response.setId(config.getId());
        response.setName(config.getName());
        response.setDescription(config.getDescription());
        response.setHp(config.getHp());
        response.setAttack(config.getAttack());
        response.setDefense(config.getDefense());
        response.setAttackSpeed(config.getAttackSpeed());
        return response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }
}
