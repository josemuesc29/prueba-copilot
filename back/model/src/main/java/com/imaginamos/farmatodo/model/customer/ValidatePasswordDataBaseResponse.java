package com.imaginamos.farmatodo.model.customer;

public class ValidatePasswordDataBaseResponse {

    private String code;
    private String message;
    private Boolean data;

    public ValidatePasswordDataBaseResponse() {
    }

    public ValidatePasswordDataBaseResponse(String code, String message, Boolean data) {
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

    public Boolean getData() {
        return data;
    }

    public void setData(Boolean data) {
        this.data = data;
    }

}
