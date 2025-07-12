package com.imaginamos.farmatodo.model.customer;

import org.json.simple.JSONObject;

public class CreditCardReq {
    private Long customerId;
    private String token;
    private String customerName;
    private String customerDocumentNumber;
    private String paymentMethod;
    private String number;
    private String maskedNumber;
    private String expirationDate;
    private String creationDate;

    public CreditCardReq(){}

    public CreditCardReq(Long customerId, String token, String customerName, String customerDocumentNumber, String paymentMethod, String number, String maskedNumber, String expirationDate, String creationDate) {
        this.customerId = customerId;
        this.token = token;
        this.customerName = customerName;
        this.customerDocumentNumber = customerDocumentNumber;
        this.paymentMethod = paymentMethod;
        this.number = number;
        this.maskedNumber = maskedNumber;
        this.expirationDate = expirationDate;
        this.creationDate = creationDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerDocumentNumber() {
        return customerDocumentNumber;
    }

    public void setCustomerDocumentNumber(String customerDocumentNumber) {
        this.customerDocumentNumber = customerDocumentNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public void setMaskedNumber(String maskedNumber) {
        this.maskedNumber = maskedNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
