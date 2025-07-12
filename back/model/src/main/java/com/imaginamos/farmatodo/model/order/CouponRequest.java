package com.imaginamos.farmatodo.model.order;


import com.imaginamos.farmatodo.model.coupon.Coupon;

public class CouponRequest {
    private Coupon.CouponType couponType;
    private Long offerId;
    private boolean hasRestriction;

    public CouponRequest(Coupon.CouponType couponType, Long offerId, boolean hasRestriction) {
        this.couponType = couponType;
        this.offerId = offerId;
        this.hasRestriction = hasRestriction;
    }

    public Coupon.CouponType getCouponType() {
        return couponType;
    }

    public void setCouponType(Coupon.CouponType couponType) {
        this.couponType = couponType;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public boolean isHasRestriction() {
        return hasRestriction;
    }

    public void setHasRestriction(boolean hasRestriction) {
        this.hasRestriction = hasRestriction;
    }
}
