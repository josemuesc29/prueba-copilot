package com.imaginamos.farmatodo.model.order;

public class OrderInfoDataResponse {
    private String status;
    private String message;
    private OrderInfoStatus data;

    public OrderInfoDataResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OrderInfoStatus getData() {
        return data;
    }

    public void setData(OrderInfoStatus data) {
        this.data = data;
    }

 /*   @Override
    public String toString() {
        return "OrderInfoDataResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }*/
}
