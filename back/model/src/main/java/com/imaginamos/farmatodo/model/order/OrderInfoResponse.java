package com.imaginamos.farmatodo.model.order;

public class OrderInfoResponse {

    private String code;
    private String message;
    private OrderInfoAmplitude data;

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public OrderInfoAmplitude getData() { return data; }

    public void setData(OrderInfoAmplitude data) { this.data = data; }
}
