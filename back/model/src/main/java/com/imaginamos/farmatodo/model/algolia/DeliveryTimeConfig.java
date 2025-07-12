package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 19/11/2018.
 */
public class DeliveryTimeConfig {
    private String deliveryType;
    private String deliveryTime;

    public DeliveryTimeConfig() {
    }

    public DeliveryTimeConfig(String deliveryType, String deliveryTime) {
        this.deliveryType = deliveryType;
        this.deliveryTime = deliveryTime;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}
