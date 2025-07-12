package com.imaginamos.farmatodo.model.order;

import com.imaginamos.farmatodo.model.payment.OrderStatusEnum;

public class CancelStatusRx {

    private String orderId;
    private OrderStatusEnum status;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }
}
