package com.imaginamos.farmatodo.model.customer;

import java.io.Serializable;

public class CustomerResetPasswordRes implements Serializable {
    private String code;
    private String message;
    private CustomerResponse data;

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

    public CustomerResponse getData() { return data; }

    public void setData(CustomerResponse data) { this.data = data; }
}
