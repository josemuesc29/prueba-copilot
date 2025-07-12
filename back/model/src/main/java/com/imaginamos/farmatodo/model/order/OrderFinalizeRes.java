package com.imaginamos.farmatodo.model.order;

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

    public List<Long> getOrders() {
        return orders;
    }

    public void setOrders(List<Long> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "OrderFinalizeRes{" +
                "message='" + message + '\'' +
                ", orders=" + orders +
                '}';
    }
}
