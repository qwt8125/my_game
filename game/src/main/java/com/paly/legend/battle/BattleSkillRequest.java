package com.paly.legend.battle;

import javax.validation.constraints.NotBlank;

public class BattleSkillRequest {

    @NotBlank
    private String skillId;
    private String targetId;

    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}
