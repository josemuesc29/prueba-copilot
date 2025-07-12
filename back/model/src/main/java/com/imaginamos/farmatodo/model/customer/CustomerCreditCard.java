package com.imaginamos.farmatodo.model.customer;

import java.util.List;

public class CustomerCreditCard {
    private String code;
    private String message;
    private List<CreditCard> data;

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

    public List<CreditCard> getData() {
        return data;
    }

    public void setData(List<CreditCard> data) {
        this.data = data;
    }
}
