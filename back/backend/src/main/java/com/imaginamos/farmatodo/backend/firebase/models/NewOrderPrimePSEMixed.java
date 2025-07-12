package com.imaginamos.farmatodo.backend.firebase.models;

public class NewOrderPrimePSEMixed {
    private String orderMixPrimeId;
    private Boolean prime;

    public String getOrderMixPrimeId() {
        return orderMixPrimeId;
    }

    public void setOrderMixPrimeId(String orderMixPrimeId) {
        this.orderMixPrimeId = orderMixPrimeId;
    }

    public Boolean getPrime() {
        return prime;
    }

    public void setPrime(Boolean prime) {
        this.prime = prime;
    }

    public NewOrderPrimePSEMixed(String orderMixPrimeId, Boolean prime) {
        this.orderMixPrimeId = orderMixPrimeId;
        this.prime = prime;
    }

    @Override
    public String toString() {
        return "NewOrderPrimePSEMixed{" +
                "orderMixPrimeId='" + orderMixPrimeId + '\'' +
                ", prime=" + prime +
                '}';
    }
}
