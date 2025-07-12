package com.imaginamos.farmatodo.model.customer;

public class CustomerBackend3 {
    private String code;
    private String message;
    private CustomerDataResponse data;

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

    public CustomerDataResponse getData() {
        return data;
    }

    public void setData(CustomerDataResponse data) {
        this.data = data;
    }
}
