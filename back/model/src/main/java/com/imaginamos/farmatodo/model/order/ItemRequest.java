package com.imaginamos.farmatodo.model.order;

public class ItemRequest {
    private Long item;
    private int quantityRequested;

    public Long getItem() {
        return item;
    }

    public void setItem(Long item) {
        this.item = item;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(int quantityRequested) {
        this.quantityRequested = quantityRequested;
    }
}
