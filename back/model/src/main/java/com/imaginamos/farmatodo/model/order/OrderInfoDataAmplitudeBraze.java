package com.imaginamos.farmatodo.model.order;

public class OrderInfoDataAmplitudeBraze {

    private String orderId;
    private String deliveryType;
    private String emailCustomer;

    public OrderInfoDataAmplitudeBraze() {
    }

    public OrderInfoDataAmplitudeBraze(String orderId, String deliveryType, String emailCustomer) {
        this.orderId = orderId;
        this.deliveryType = deliveryType;
        this.emailCustomer = emailCustomer;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getEmailCustomer() {
        return emailCustomer;
    }

    public void setEmailCustomer(String emailCustomer) {
        this.emailCustomer = emailCustomer;
    }
}
