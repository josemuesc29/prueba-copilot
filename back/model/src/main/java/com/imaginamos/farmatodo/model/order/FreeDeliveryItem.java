package com.imaginamos.farmatodo.model.order;

import java.io.Serializable;

public class FreeDeliveryItem implements Serializable {

    private Long itemId;
    private Integer quantity;

    public FreeDeliveryItem(Long itemId, Integer quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
