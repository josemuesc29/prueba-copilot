package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.order.DeliveryOrder;

import java.util.List;

public class AnswerDeleteCart {
    private String message;
    private DeliveryOrder deliveryOrder;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }
}
