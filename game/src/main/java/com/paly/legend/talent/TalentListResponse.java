package com.paly.legend.talent;

import java.util.ArrayList;
import java.util.List;

public class TalentListResponse {

    private int totalPoints;
    private int usedPoints;
    private int availablePoints;
    private int resetGoldCost;
    private List<TalentResponse> talents = new ArrayList<TalentResponse>();

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public int getUsedPoints() { return usedPoints; }
    public void setUsedPoints(int usedPoints) { this.usedPoints = usedPoints; }
    public int getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(int availablePoints) { this.availablePoints = availablePoints; }
    public int getResetGoldCost() { return resetGoldCost; }
    public void setResetGoldCost(int resetGoldCost) { this.resetGoldCost = resetGoldCost; }
    public List<TalentResponse> getTalents() { return talents; }
    public void setTalents(List<TalentResponse> talents) { this.talents = talents; }
}
