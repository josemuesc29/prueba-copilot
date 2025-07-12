package com.imaginamos.farmatodo.model.order;

public class DataEventResponseAmplitude {
    private String orderId;

    public DataEventResponseAmplitude() {
    }

    public DataEventResponseAmplitude(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "DataEventResponseAmplitude{" +
                "orderId='" + orderId + '\'' +
                '}';
    }
}
