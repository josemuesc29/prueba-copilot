package com.imaginamos.farmatodo.model.order;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.io.Serializable;

public class ValidateOrderBackend3 implements Serializable {
    private String code;
    private String message;
    private OrderJson data;

    public ValidateOrderBackend3(){}

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

    public OrderJson getData() {
        return data;
    }

    public void setData(OrderJson data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
