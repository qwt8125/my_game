package com.paly.legend.task;

import java.util.ArrayList;
import java.util.List;

public class TaskRewardResponse {

    private int exp;
    private int gold;
    private List<TaskRewardItemResponse> items = new ArrayList<TaskRewardItemResponse>();

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

    public List<TaskRewardItemResponse> getItems() {
        return items;
    }

    public void setItems(List<TaskRewardItemResponse> items) {
        this.items = items;
    }
}
