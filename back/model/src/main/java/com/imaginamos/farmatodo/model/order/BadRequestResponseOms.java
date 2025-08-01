package com.imaginamos.farmatodo.model.order;

public class BadRequestResponseOms {
    private String code;
    private String message;

    public BadRequestResponseOms() {
    }

    public BadRequestResponseOms(String code, String message) {
        this.code = code;
        this.message = message;
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

    @Override
    public String toString() {
        return "BadRequestResponseOms{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
