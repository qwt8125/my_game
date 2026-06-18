package com.paly.legend.battle;

import java.util.ArrayList;
import java.util.List;

public class BattleActionResponse {

    private int round;
    private String actor;
    private String action;
    private String target;
    private int damage;
    private int targetHpAfter;
    private String targetType;
    private List<BattleActionTargetResponse> targets = new ArrayList<BattleActionTargetResponse>();
    private String message;

    public BattleActionResponse() {
    }

    public BattleActionResponse(int round, String actor, String action, String target,
                                int damage, int targetHpAfter, String message) {
        this.round = round;
        this.actor = actor;
        this.action = action;
        this.target = target;
        this.damage = damage;
        this.targetHpAfter = targetHpAfter;
        this.message = message;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getTargetHpAfter() {
        return targetHpAfter;
    }

    public void setTargetHpAfter(int targetHpAfter) {
        this.targetHpAfter = targetHpAfter;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public List<BattleActionTargetResponse> getTargets() {
        return targets;
    }

    public void setTargets(List<BattleActionTargetResponse> targets) {
        this.targets = targets == null ? new ArrayList<BattleActionTargetResponse>() : targets;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
