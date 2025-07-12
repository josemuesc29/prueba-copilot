package com.imaginamos.farmatodo.model.order;


public class FreeDeliveryCoupon {

    private String couponType;
    private Long offerId;

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public FreeDeliveryCoupon(String couponType, Long offerId) {
        this.couponType = couponType;
        this.offerId = offerId;
    }

    public FreeDeliveryCoupon() {
    }
}
