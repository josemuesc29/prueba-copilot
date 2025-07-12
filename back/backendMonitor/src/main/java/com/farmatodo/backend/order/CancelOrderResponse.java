package com.farmatodo.backend.order;

public class CancelOrderResponse {
    private String language;
    private String code;
    private String message;

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
