package com.paly.legend.skill;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class SkillSlotRequest {

    @NotBlank
    private String skillId;

    @Min(0)
    @Max(4)
    private int skillSlot;

    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public int getSkillSlot() { return skillSlot; }
    public void setSkillSlot(int skillSlot) { this.skillSlot = skillSlot; }
}
