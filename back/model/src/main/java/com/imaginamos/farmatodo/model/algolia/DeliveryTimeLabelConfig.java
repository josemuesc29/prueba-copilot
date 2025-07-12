package com.imaginamos.farmatodo.model.algolia;


import com.google.appengine.repackaged.com.google.gson.Gson;

public class DeliveryTimeLabelConfig {

    public DeliveryTimeLabelConfig() {
    }

    public DeliveryTimeLabelConfig(DeliveryTimeLabelCart deliveryTimeLabelCart, DeliveryTimeLabelGeneric deliveryTimeLabelGeneric, String objectID) {
        this.deliveryTimeLabelCart = deliveryTimeLabelCart;
        this.deliveryTimeLabelGeneric = deliveryTimeLabelGeneric;
        this.objectID = null;
    }

    private DeliveryTimeLabelCart deliveryTimeLabelCart;
    private DeliveryTimeLabelGeneric deliveryTimeLabelGeneric;
    private String objectID;

    public DeliveryTimeLabelCart getDeliveryTimeLabelCart() {
        return deliveryTimeLabelCart;
    }

    public void setDeliveryTimeLabelCart(DeliveryTimeLabelCart deliveryTimeLabelCart) {
        this.deliveryTimeLabelCart = deliveryTimeLabelCart;
    }

    public DeliveryTimeLabelGeneric getDeliveryTimeLabelGeneric() {
        return deliveryTimeLabelGeneric;
    }

    public void setDeliveryTimeLabelGeneric(DeliveryTimeLabelGeneric deliveryTimeLabelGeneric) {
        this.deliveryTimeLabelGeneric = deliveryTimeLabelGeneric;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = null;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
