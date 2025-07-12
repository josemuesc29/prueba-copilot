package com.imaginamos.farmatodo.model.order;

public class OrderInfoAmpOMSResponse {

    private String code;
    private String message;
    private OrderInfoAmplitudeOMS data;

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public OrderInfoAmplitudeOMS getData() { return data; }

    public void setData(OrderInfoAmplitudeOMS data) { this.data = data; }
}
