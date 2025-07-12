package com.imaginamos.farmatodo.model.customer;

public class SubscribePrimeFreeDaysRequest {

    private Long creditCardId;
    private Long freeDays;
    private String planType;
    private Long customerId;

    private String deviceId;

    public SubscribePrimeFreeDaysRequest() {
    }

    public SubscribePrimeFreeDaysRequest(Long creditCardId, Long freeDays, String planType, Long customerId) {
        this.creditCardId = creditCardId;
        this.freeDays = freeDays;
        this.planType = planType;
        this.customerId = customerId;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public Long getFreeDays() {
        return freeDays;
    }

    public void setFreeDays(Long freeDays) {
        this.freeDays = freeDays;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
