package com.imaginamos.farmatodo.model.coupon;

public enum MessageTypesCoupon {
    GENERIC_MESSAGE("GENERIC_MESSAGE"),
    GENERIC_WITH_COUPON("ERROR_COUPON_FILTER_CARD_BIN");

    MessageTypesCoupon(String error_coupon_filter_card_bin) {
    }
    private String couponTypeMessage;

    public String getCouponTypeMessage() {
        return this.couponTypeMessage;
    }
    public void setCouponTypeMessage(String couponTypeMessage) {
        this.couponTypeMessage = couponTypeMessage;
    }


}