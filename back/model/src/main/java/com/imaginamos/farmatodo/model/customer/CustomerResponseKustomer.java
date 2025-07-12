package com.imaginamos.farmatodo.model.customer;

public class CustomerResponseKustomer {
    private String message;
    private String code;

    public CustomerResponseKustomer(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public CustomerResponseKustomer() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
