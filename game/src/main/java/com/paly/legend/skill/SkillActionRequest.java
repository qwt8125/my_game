package com.paly.legend.skill;

import javax.validation.constraints.NotBlank;

public class SkillActionRequest {
    @NotBlank
    private String skillId;

    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
}
