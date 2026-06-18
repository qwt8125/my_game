package com.paly.legend.talent;

import javax.validation.constraints.NotBlank;

public class TalentActionRequest {
    @NotBlank
    private String talentId;

    public String getTalentId() { return talentId; }
    public void setTalentId(String talentId) { this.talentId = talentId; }
}
