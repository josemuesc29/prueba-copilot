package com.imaginamos.farmatodo.model.item;

import java.util.List;

public class ListItemToUpdatePriceRequest {
    private List<ItemToUpdatePrice> items;


    public List<ItemToUpdatePrice> getItems() {
        return items;
    }

    public void setItems(List<ItemToUpdatePrice> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ListItemToUpdatePriceRequest{" +
                "items=" + items +
                '}';
    }
}
