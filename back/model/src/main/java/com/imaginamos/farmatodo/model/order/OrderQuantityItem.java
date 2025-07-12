package com.imaginamos.farmatodo.model.order;

public class OrderQuantityItem {

    private Long itemId;
    private Long quantity;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderQuantityItem{" +
                "itemId=" + itemId +
                ", quantity=" + quantity +
                '}';
    }
}
