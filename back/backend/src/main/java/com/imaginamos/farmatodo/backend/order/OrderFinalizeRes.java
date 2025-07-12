package com.imaginamos.farmatodo.backend.order;

import com.imaginamos.farmatodo.model.order.OrderFinalizedCustomerRes;

import java.util.List;

public class OrderFinalizeRes {
    String message;
    List<Long> orders;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOrders(List<Long> orders) {
        this.orders = orders;
    }

    public List<Long> getOrders() {
        return orders;
    }

    @Override
    public String toString() {
        return "OrderFinalizeRes{" +
                "message='" + message + '\'' +
                ", orders=" + orders +
                '}';
    }
}
