package com.imaginamos.farmatodo.model.customer;

public class GetCustomerPrimeCartResponse {
    public String code;
    public String message;
    public CustomerCreditCardPrimeData data;

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

    public CustomerCreditCardPrimeData getData() {
        return data;
    }

    public void setData(CustomerCreditCardPrimeData data) {
        this.data = data;
    }
}
