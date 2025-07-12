package com.imaginamos.farmatodo.model.firebase;

public class AddFirebaseCodeLoginRequest {

    private String code;
    private String customerId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
