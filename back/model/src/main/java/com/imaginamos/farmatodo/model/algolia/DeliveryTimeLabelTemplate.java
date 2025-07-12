package com.imaginamos.farmatodo.model.algolia;

public class DeliveryTimeLabelTemplate {

    private DeliveryTimeLabelWeb deliveryTimeLabelWeb;
    private DeliveryTimeLabelMobile deliveryTimeLabelMobile;
    private String objectID;

    public DeliveryTimeLabelWeb getDeliveryTimeLabelWeb() {
        return deliveryTimeLabelWeb;
    }

    public void setDeliveryTimeLabelWeb(DeliveryTimeLabelWeb deliveryTimeLabelWeb) {
        this.deliveryTimeLabelWeb = deliveryTimeLabelWeb;
    }

    public DeliveryTimeLabelMobile getDeliveryTimeLabelMobile() {
        return deliveryTimeLabelMobile;
    }

    public void setDeliveryTimeLabelMobile(DeliveryTimeLabelMobile deliveryTimeLabelMobile) {
        this.deliveryTimeLabelMobile = deliveryTimeLabelMobile;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
