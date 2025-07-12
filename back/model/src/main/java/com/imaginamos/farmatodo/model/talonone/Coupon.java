package com.imaginamos.farmatodo.model.talonone;

public class Coupon {
    private String couponMessage;
    private String talonOneOfferDescription;
    private String nameCoupon;
    private Double discountCoupon;
    private String typeNotificacion;

    public String getCouponMessage() {
        return couponMessage;
    }

    public void setCouponMessage(String couponMessage) {
        this.couponMessage = couponMessage;
    }

    public String getTalonOneOfferDescription() {
        return talonOneOfferDescription;
    }

    public void setTalonOneOfferDescription(String talonOneOfferDescription) {
        this.talonOneOfferDescription = talonOneOfferDescription;
    }

    public String getNameCoupon() {
        return nameCoupon;
    }

    public void setNameCoupon(String nameCoupon) {
        this.nameCoupon = nameCoupon;
    }

    public Double getDiscountCoupon() {
        return discountCoupon;
    }

    public void setDiscountCoupon(Double discountCoupon) {
        this.discountCoupon = discountCoupon;
    }

    public String getTypeNotificacion() {
        return typeNotificacion;
    }

    public void setTypeNotificacion(String typeNotificacion) {
        this.typeNotificacion = typeNotificacion;
    }
}
