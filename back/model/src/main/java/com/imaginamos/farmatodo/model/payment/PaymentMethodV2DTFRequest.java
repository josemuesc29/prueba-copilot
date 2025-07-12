package com.imaginamos.farmatodo.model.payment;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class PaymentMethodV2DTFRequest {
    private long customerId;
    private String os;
    private String deliveryType;
    private String version;


    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
