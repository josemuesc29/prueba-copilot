package com.imaginamos.farmatodo.model.order;

public class OrderInfoAmplitudeBraze {

    private String code;
    private String message;
    private OrderInfoDataAmplitudeBraze data;

    public OrderInfoAmplitudeBraze() {
    }

    public OrderInfoAmplitudeBraze(String code, String message, OrderInfoDataAmplitudeBraze data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OrderInfoDataAmplitudeBraze getData() {
        return data;
    }

    public void setData(OrderInfoDataAmplitudeBraze data) {
        this.data = data;
    }
}
