package com.imaginamos.farmatodo.model.order;

import java.util.List;

public class GetUnbilledItemsByOrderResponseData {

    private List<Long> items;

    public List<Long> getItems() {
        return items;
    }

    public void setItems(List<Long> items) {
        this.items = items;
    }
}
