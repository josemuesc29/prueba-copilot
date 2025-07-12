package com.imaginamos.farmatodo.model.coupon;

public class CustomerCouponStoreReq {

    private Long documentNumber;
    private String coupon;

    public Long getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Long documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }
}
