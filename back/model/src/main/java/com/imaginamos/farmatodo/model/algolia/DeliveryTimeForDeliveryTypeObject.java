package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class DeliveryTimeForDeliveryTypeObject {

    private List<DeliveryTimeForDeliveryTypeElement> deliveryTimeForDeliveryType;
    private String objectID;

    public List<DeliveryTimeForDeliveryTypeElement> getDeliveryTimeForDeliveryType() {
        return deliveryTimeForDeliveryType;
    }

    public void setDeliveryTimeForDeliveryType(List<DeliveryTimeForDeliveryTypeElement> deliveryTimeForDeliveryType) {
        this.deliveryTimeForDeliveryType = deliveryTimeForDeliveryType;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
