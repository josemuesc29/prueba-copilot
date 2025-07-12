package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName;

public class UpdateEmailCustomerResponse {
    private String code;
    private String message;

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

    @Override
    public String toString() {
        return "UpdateEmailCustomerResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
