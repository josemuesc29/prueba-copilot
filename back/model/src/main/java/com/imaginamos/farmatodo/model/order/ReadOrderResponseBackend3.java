package com.imaginamos.farmatodo.model.order;

public class ReadOrderResponseBackend3 {
    private String code;
    private String message;
    private ReadOrderResponse data;

    public ReadOrderResponseBackend3(String code, String message, ReadOrderResponse data) {
        this.code = code;
        this.message = message;
        this.data = data;
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

    public ReadOrderResponse getData() {
        return data;
    }

    public void setData(ReadOrderResponse data) {
        this.data = data;
    }
}
