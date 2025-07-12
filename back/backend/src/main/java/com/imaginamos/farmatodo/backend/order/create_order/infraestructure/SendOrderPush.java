package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

public class SendOrderPush {
    private String email;
    private int status;

    private String statusBraze;
    private String orderId;

    private Long cancellationReason;

    public SendOrderPush() {
    }

    public SendOrderPush(String email, int status, String orderId, Long cancellationReason) {
        this.email = email;
        this.status = status;
        this.orderId = orderId;
        this.cancellationReason = cancellationReason;
    }

    public SendOrderPush(String email, int status, String statusBraze, String orderId, Long cancellationReason) {
        this.email = email;
        this.status = status;
        this.statusBraze = statusBraze;
        this.orderId = orderId;
        this.cancellationReason = cancellationReason;
    }

    public String getEmail() {
        return email;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusBraze() {
        return statusBraze;
    }

    public void setStatusBraze(String statusBraze) {
        this.statusBraze = statusBraze;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(Long cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
