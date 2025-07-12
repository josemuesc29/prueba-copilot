package com.imaginamos.farmatodo.model.customer;

public class CreditCardDefaultReq {
    private Long creditCardId;
    private Long customerId;

    public CreditCardDefaultReq() {
    }

    public CreditCardDefaultReq(Long creditCardId, Long customerId) {
        this.creditCardId = creditCardId;
        this.customerId = customerId;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
