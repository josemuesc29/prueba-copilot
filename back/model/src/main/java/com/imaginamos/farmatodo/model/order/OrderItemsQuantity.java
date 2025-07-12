package com.imaginamos.farmatodo.model.order;

public class OrderItemsQuantity {
    private Long id;
    private int quantity;

    public OrderItemsQuantity() {
    }

    public OrderItemsQuantity(Long id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
