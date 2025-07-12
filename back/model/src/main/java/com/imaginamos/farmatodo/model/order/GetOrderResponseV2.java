package com.imaginamos.farmatodo.model.order;

public class GetOrderResponseV2 {
    private String code;
    private String message;
    private DeliveryOrderV2 data;

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

    public DeliveryOrderV2 getData() {
        return data;
    }

    public void setData(DeliveryOrderV2 data) {
        this.data = data;
    }
}
