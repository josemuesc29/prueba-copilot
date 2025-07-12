package com.imaginamos.farmatodo.model.monitor;


public class OrderResponse {
    private Long orderId;
    private Long id;
    private String statusName;
    private String createDate;
    private String ordersAssigned;

    public Long getOrderId() {
        return orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getOrdersAssigned() {
        return ordersAssigned;
    }

    public void setOrdersAssigned(String ordersAssigned) {
        this.ordersAssigned = ordersAssigned;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "orderId=" + orderId +
                ", id=" + id +
                ", statusName='" + statusName + '\'' +
                ", createDate='" + createDate + '\'' +
                ", ordersAssigned='" + ordersAssigned + '\'' +
                '}';
    }
}
