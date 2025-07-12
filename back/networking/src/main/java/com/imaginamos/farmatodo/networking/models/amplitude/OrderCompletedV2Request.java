package com.imaginamos.farmatodo.networking.models.amplitude;

public class OrderCompletedV2Request {
    private String orderId;
    public OrderCompletedV2Request(String orderId) {
        this.orderId = orderId;
    }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

}
