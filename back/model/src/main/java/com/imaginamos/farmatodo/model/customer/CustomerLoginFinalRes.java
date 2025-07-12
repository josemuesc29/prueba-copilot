package com.imaginamos.farmatodo.model.customer;

import java.util.List;

public class CustomerLoginFinalRes {
    private String code;
    private String message;
    private List<CustomerNewLoginRes> data;

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

    public List<CustomerNewLoginRes> getData() {
        return data;
    }

    public void setData(List<CustomerNewLoginRes> data) {
        this.data = data;
    }
}
