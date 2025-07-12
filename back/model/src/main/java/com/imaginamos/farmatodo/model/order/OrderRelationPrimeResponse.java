package com.imaginamos.farmatodo.model.order;

public class OrderRelationPrimeResponse {

    private String code;
    private String message;
    private String data;

    public OrderRelationPrimeResponse() {
    }

    public OrderRelationPrimeResponse(String code, String message, String data) {
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
