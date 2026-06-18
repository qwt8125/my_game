package com.paly.legend.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

public class SellMaterialsRequest {

    @Valid
    @NotEmpty
    private List<SellMaterialsRequestItem> items = new ArrayList<SellMaterialsRequestItem>();

    public List<SellMaterialsRequestItem> getItems() {
        return items;
    }

    public void setItems(List<SellMaterialsRequestItem> items) {
        this.items = items;
    }
}
