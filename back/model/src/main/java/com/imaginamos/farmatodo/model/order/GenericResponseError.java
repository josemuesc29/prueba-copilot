package com.imaginamos.farmatodo.model.order;


public class GenericResponseError {
    private String code;
    private String message;


    public GenericResponseError() {

    }

    public GenericResponseError(String code, String message) {
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


}
