package com.paly.legend.inventory;

import java.util.ArrayList;
import java.util.List;

public class InventoryListResponse {

    private int capacity;
    private int usedSlots;
    private int remainingSlots;
    private List<InventoryItemResponse> items = new ArrayList<InventoryItemResponse>();

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getUsedSlots() {
        return usedSlots;
    }

    public void setUsedSlots(int usedSlots) {
        this.usedSlots = usedSlots;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    public List<InventoryItemResponse> getItems() {
        return items;
    }

    public void setItems(List<InventoryItemResponse> items) {
        this.items = items;
    }
}
