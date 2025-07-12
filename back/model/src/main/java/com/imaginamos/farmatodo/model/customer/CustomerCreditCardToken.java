package com.imaginamos.farmatodo.model.customer;


public class CustomerCreditCardToken {
    private String customerSession;
    private String customerId;
    private String country;
    private String callbackUrl;
    private String createdAt;

    public CustomerCreditCardToken() {
    }

    public String getCustomerSession() {
        return customerSession;
    }

    public void setCustomerSession(String customerSession) {
        this.customerSession = customerSession;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return "CustomerCreditCardToken{" +
                "customerSession='" + customerSession + '\'' +
                ", customerId='" + customerId + '\'' +
                ", country='" + country + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
