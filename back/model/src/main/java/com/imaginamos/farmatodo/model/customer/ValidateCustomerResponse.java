package com.imaginamos.farmatodo.model.customer;

public class ValidateCustomerResponse {
    private String code;
    private String message;
    private ValidateCustomerDocumentNumber data;

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

    public ValidateCustomerDocumentNumber getData() {
        return data;
    }

    public void setData(ValidateCustomerDocumentNumber data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ValidateCustomerResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
