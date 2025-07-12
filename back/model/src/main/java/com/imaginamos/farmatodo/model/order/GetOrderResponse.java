package com.imaginamos.farmatodo.model.order;

public class GetOrderResponse {
    private String code;
    private String message;
    private DeliveryOrder data;

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

    public DeliveryOrder getData() {
        return data;
    }

    public void setData(DeliveryOrder data) {
        this.data = data;
    }
}
