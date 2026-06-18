package com.paly.legend.activity;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.config.ActivityConfig;
import com.paly.legend.config.ActivityEffectConfig;
import com.paly.legend.config.GameConfigService;
import org.springframework.stereotype.Service;

@Service
public class ActivityEffectService {

    public static final String BATTLE_EXP = "battle_exp";
    public static final String BATTLE_GOLD = "battle_gold";
    public static final String DROP_RATE = "drop_rate";
    public static final String IDLE_EXP = "idle_exp";
    public static final String IDLE_GOLD = "idle_gold";
    public static final String WORLD_BOSS_GOLD = "world_boss_gold";

    private final GameConfigService gameConfigService;

    public ActivityEffectService(GameConfigService gameConfigService) {
        this.gameConfigService = gameConfigService;
    }

    public int activePercent(String type) {
        if (type == null || type.trim().isEmpty()) {
            return 0;
        }
        int percent = 0;
        for (ActivityConfig activity : gameConfigService.listActivities()) {
            if (!"active".equals(activity.getStatus()) || activity.getEffects() == null) {
                continue;
            }
            for (ActivityEffectConfig effect : activity.getEffects()) {
                if (effect != null && type.equals(effect.getType())) {
                    percent += Math.max(0, effect.getPercent());
                }
            }
        }
        return percent;
    }

    public int bonusAmount(int baseAmount, String type) {
        if (baseAmount <= 0) {
            return 0;
        }
        int percent = activePercent(type);
        if (percent <= 0) {
            return 0;
        }
        return Math.max(1, baseAmount * percent / 100);
    }

    public double rateMultiplier(String type) {
        int percent = activePercent(type);
        if (percent <= 0) {
            return 1.0;
        }
        return 1.0 + percent / 100.0;
    }

    public List<ActivityEffectResponse> responses(ActivityConfig activity) {
        List<ActivityEffectResponse> result = new ArrayList<ActivityEffectResponse>();
        if (activity == null || activity.getEffects() == null) {
            return result;
        }
        for (ActivityEffectConfig effect : activity.getEffects()) {
            if (effect == null || effect.getType() == null || effect.getType().trim().isEmpty()
                    || effect.getPercent() <= 0) {
                continue;
            }
            result.add(new ActivityEffectResponse(effect.getType(), effect.getPercent(), effect.getDescription()));
        }
        return result;
    }
}
