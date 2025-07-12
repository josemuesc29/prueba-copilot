package com.imaginamos.farmatodo.backend.firebase.models;

public class NotifyCodeLogin {
    private String key;
    private String code;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public NotifyCodeLogin(String key, String code) {
        this.key = key;
        this.code = code;
    }
}
