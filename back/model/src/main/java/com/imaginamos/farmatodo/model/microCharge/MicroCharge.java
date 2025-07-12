package com.imaginamos.farmatodo.model.microCharge;

public class MicroCharge {

    private Long customerId;
    private Long paymentMethodId;
    private Long customerPaymentCardId;
    private Integer valueMicroCharge;
    private boolean tokenization;
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Long getCustomerPaymentCardId() {
        return customerPaymentCardId;
    }

    public void setCustomerPaymentCardId(Long customerPaymentCardId) {
        this.customerPaymentCardId = customerPaymentCardId;
    }

    public Integer getValueMicroCharge() {
        return valueMicroCharge;
    }

    public void setValueMicroCharge(Integer valueMicroCharge) {
        this.valueMicroCharge = valueMicroCharge;
    }

    public boolean isTokenization() {
        return tokenization;
    }

    public void setTokenization(boolean tokenization) {
        this.tokenization = tokenization;
    }
}
