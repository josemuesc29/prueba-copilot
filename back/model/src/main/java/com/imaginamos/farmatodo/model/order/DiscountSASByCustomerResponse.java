package com.imaginamos.farmatodo.model.order;

public class DiscountSASByCustomerResponse {

    private String code;
    private String message;
    private DiscountSASByCustomerData data;

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

    public DiscountSASByCustomerData getData() {
        return data;
    }

    public void setData(DiscountSASByCustomerData data) {
        this.data = data;
    }
}
