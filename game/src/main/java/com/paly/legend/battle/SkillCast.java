package com.paly.legend.battle;

import com.paly.legend.config.SkillConfig;

class SkillCast {

    private final SkillConfig skill;
    private final int level;
    private final boolean manual;

    SkillCast(SkillConfig skill, int level) {
        this(skill, level, false);
    }

    SkillCast(SkillConfig skill, int level, boolean manual) {
        this.skill = skill;
        this.level = level;
        this.manual = manual;
    }

    SkillConfig getSkill() {
        return skill;
    }

    int getLevel() {
        return level;
    }

    boolean isManual() {
        return manual;
    }
}
