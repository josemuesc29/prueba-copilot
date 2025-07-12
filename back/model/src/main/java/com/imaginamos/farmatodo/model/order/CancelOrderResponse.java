package com.imaginamos.farmatodo.model.order;

public class CancelOrderResponse {
    private String language;
    private String code;
    private String message;
    private CancelOrderResponseData data;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

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

    public CancelOrderResponseData getData() {
        return data;
    }

    public void setData(CancelOrderResponseData data) {
        this.data = data;
    }
}
