package com.imaginamos.farmatodo.model.customer;


public class CustomerPrimeFreeDays {

    private String token;
    private String tokenIdWebSafe;
    private String idCustomerWebSafe;
    private Long creditCardId;
    private String planType;

    private String deviceId;

    public CustomerPrimeFreeDays() {
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {

        this.planType = planType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public String getIdCustomerWebSafe() {
        return idCustomerWebSafe;
    }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) {
        this.idCustomerWebSafe = idCustomerWebSafe;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
