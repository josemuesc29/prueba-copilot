package com.imaginamos.farmatodo.model.order;

public class ValidateFreeDeliveryByCartResponse {

    private String code;
    private String message;
    private Boolean data;

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

    public Boolean getData() {
        return data;
    }

    public void setData(Boolean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ValidateFreeDeliveryByCartResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
