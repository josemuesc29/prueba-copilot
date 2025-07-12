package com.imaginamos.farmatodo.model.order;

public class OrderCharge {

    private RequestSourceEnum source;
    private Long orderId;
    private String uuid;

    public RequestSourceEnum getSource() {
        return source;
    }

    public void setSource(RequestSourceEnum source) {
        this.source = source;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "OrderCharge{" +
                "source=" + source +
                ", orderId=" + orderId +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
