package com.imaginamos.farmatodo.model.monitor;

public class OrderItem {
    private long itemId;
    private int quantityRequested;

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(int quantityRequested) {
        this.quantityRequested = quantityRequested;
    }
}
