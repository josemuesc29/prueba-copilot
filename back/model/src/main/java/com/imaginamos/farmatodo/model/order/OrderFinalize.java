package com.imaginamos.farmatodo.model.order;

public class OrderFinalize {
    String KeyClient;
    Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getKeyClient() {
        return KeyClient;
    }

    public void setKeyClient(String keyClient) {
        KeyClient = keyClient;
    }

    @Override
    public String toString() {
        return "OrderFinalize{" +
                "KeyClient='" + KeyClient + '\'' +
                ", orderId=" + orderId +
                '}';
    }
}
