package com.imaginamos.farmatodo.model.coupon;

public class CouponFilterValidateResponse {
    private int status;
    private String couponName;
    private ValidationMessage message;


    public ValidationMessage getMessage() {
        return message;
    }

    public void setMessage(ValidationMessage message) {
        this.message = message;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}



