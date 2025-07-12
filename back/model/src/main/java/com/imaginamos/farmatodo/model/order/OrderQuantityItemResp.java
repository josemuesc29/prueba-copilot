package com.imaginamos.farmatodo.model.order;

import java.util.List;

public class OrderQuantityItemResp {

    private List<OrderQuantityItem> itemQuantity;

    public List<OrderQuantityItem> getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(List<OrderQuantityItem> itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    @Override
    public String toString() {
        return "OrderQuantityItemResp{" +
                "itemQuantity=" + itemQuantity +
                '}';
    }
}
