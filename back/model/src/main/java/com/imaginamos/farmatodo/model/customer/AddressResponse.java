package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class AddressResponse {
    private String code;
    private String message;
    private AddresResponseData data;

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

    public AddresResponseData getData() {
        return data;
    }

    public void setData(AddresResponseData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
