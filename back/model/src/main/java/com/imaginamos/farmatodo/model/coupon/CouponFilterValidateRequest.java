package com.imaginamos.farmatodo.model.coupon;



public class CouponFilterValidateRequest {
    private Long customerId;
    private Long paymentCardId;
    private Integer paymentMethodId;

    public Long getPaymentCardId() {
        return paymentCardId;
    }

    public void setPaymentCardId(Long paymentCardId) {
        this.paymentCardId = paymentCardId;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    //toString()
    @Override
    public String toString() {
        return "CouponFilterValidateRequest{" +
                "customerId=" + customerId +
                ", paymentCardId=" + paymentCardId +
                ", paymentMethodId=" + paymentMethodId +
                '}';
    }

}
