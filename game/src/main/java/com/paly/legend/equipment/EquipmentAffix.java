package com.paly.legend.equipment;

public class EquipmentAffix {

    private String stat;
    private double value;

    public EquipmentAffix() {
    }

    public EquipmentAffix(String stat, double value) {
        this.stat = stat;
        this.value = value;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
