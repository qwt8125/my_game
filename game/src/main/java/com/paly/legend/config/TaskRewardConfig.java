package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class TaskRewardConfig {

    private int exp;
    private int gold;
    private List<TaskRewardItemConfig> items = new ArrayList<TaskRewardItemConfig>();

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

    public List<TaskRewardItemConfig> getItems() {
        return items;
    }

    public void setItems(List<TaskRewardItemConfig> items) {
        this.items = items;
    }
}
