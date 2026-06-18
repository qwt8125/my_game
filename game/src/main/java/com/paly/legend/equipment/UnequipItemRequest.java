package com.paly.legend.equipment;

import javax.validation.constraints.NotBlank;

public class UnequipItemRequest {

    @NotBlank
    private String slot;

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }
}

