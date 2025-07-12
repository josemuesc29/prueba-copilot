package com.imaginamos.farmatodo.model.customer;

public class CustomerPhotoDataResponse {
    private String code;
    private String message;
    private CustomerPhotoDataList data;

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

    public CustomerPhotoDataList getData() {
        return data;
    }

    public void setData(CustomerPhotoDataList data) {
        this.data = data;
    }
}
