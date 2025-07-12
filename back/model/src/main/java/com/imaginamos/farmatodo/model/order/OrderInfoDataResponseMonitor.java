package com.imaginamos.farmatodo.model.order;

public class OrderInfoDataResponseMonitor {
    private int statusCode;
    private String status;
    private String message;
    private OrderInfoStatus data;

    public OrderInfoDataResponseMonitor(int statusCode, String status) {
        this.statusCode = statusCode;
        this.status = status;
    }

    public OrderInfoDataResponseMonitor(int statusCode, String status, String message) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
    }

    public OrderInfoDataResponseMonitor() {

    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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


    @Override
    public String toString() {
        return "OrderInfoDataResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
