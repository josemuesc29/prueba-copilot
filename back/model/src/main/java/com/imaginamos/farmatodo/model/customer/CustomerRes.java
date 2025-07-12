package com.imaginamos.farmatodo.model.customer;

public class CustomerRes {
    private String code;
    private String message;
    private CustomerJSON data;

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

    public CustomerJSON getData() {
        return data;
    }

    public void setData(CustomerJSON data) {
        this.data = data;
    }
}
