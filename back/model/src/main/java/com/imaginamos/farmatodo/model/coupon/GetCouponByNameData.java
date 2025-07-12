package com.imaginamos.farmatodo.model.coupon;

public class GetCouponByNameData {

    private String couponType;
    private Long offerId;

    public GetCouponByNameData(String couponType, Long offerId) {
        this.couponType = couponType;
        this.offerId = offerId;
    }

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
}
