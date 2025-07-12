package com.imaginamos.farmatodo.model.order;

import java.util.List;

public class OrderFinalizedReq {
    private List<OrderFinalized> orderFinalized;

    public List<OrderFinalized> getOrderFinalized() {
        return orderFinalized;
    }

    @Override
    public String toString() {
        return "OrderFinalizedReq{" +
                "orderFinalized=" + orderFinalized +
                '}';
    }
}
