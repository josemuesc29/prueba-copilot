package com.imaginamos.farmatodo.networking.talonone.model;

import com.google.gson.Gson;

public class TalonOneDeductDiscount {
    private Long orderId;
    private Double discountProductRpm;
    private Double discountProductPrime;
    private Double discountProductTalon;
    private String nameCoupon;
    private Double discountCoupon;
    private String nameAutomaticCoupon;
    private Double discountAutomaticCoupon;

    private Long usedFarmacredits;

    public TalonOneDeductDiscount() {}

    public TalonOneDeductDiscount(Long orderId, Double discountProductRpm, Double discountProductPrime, Double discountProductTalon, String nameCoupon, Double discountCoupon, Long usedFarmacredits) {
        this.orderId = orderId;
        this.discountProductRpm = discountProductRpm;
        this.discountProductPrime = discountProductPrime;
        this.discountProductTalon = discountProductTalon;
        this.nameCoupon = nameCoupon;
        this.discountCoupon = discountCoupon;
        this.usedFarmacredits = usedFarmacredits;
    }

    public Long getUsedFarmacredits() {
        return usedFarmacredits;
    }

    public void setUsedFarmacredits(Long usedFarmacredits) {
        this.usedFarmacredits = usedFarmacredits;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getDiscountProductRpm() {
        return discountProductRpm;
    }

    public void setDiscountProductRpm(Double discountProductRpm) {
        this.discountProductRpm = discountProductRpm;
    }

    public Double getDiscountProductPrime() {
        return discountProductPrime;
    }

    public void setDiscountProductPrime(Double discountProductPrime) {
        this.discountProductPrime = discountProductPrime;
    }

    public Double getDiscountProductTalon() {
        return discountProductTalon;
    }

    public void setDiscountProductTalon(Double discountProductTalon) {
        this.discountProductTalon = discountProductTalon;
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

    public String getNameAutomaticCoupon() {
        return nameAutomaticCoupon;
    }

    public void setNameAutomaticCoupon(String nameAutomaticCoupon) {
        this.nameAutomaticCoupon = nameAutomaticCoupon;
    }

    public Double getDiscountAutomaticCoupon() {
        return discountAutomaticCoupon;
    }

    public void setDiscountAutomaticCoupon(Double discountAutomaticCoupon) {
        this.discountAutomaticCoupon = discountAutomaticCoupon;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
