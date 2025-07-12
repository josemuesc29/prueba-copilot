package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

/**
 * Created by JPuentes on 19/11/2018.
 */
public class DeliveryTypeTime {

    private String objectID;
    private List<List<String>> deliveryTimes;

    public DeliveryTypeTime() {
    }

    public DeliveryTypeTime(String objectID, List<List<String>> deliveryTimes) {
        this.objectID = objectID;
        this.deliveryTimes = deliveryTimes;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public List<List<String>> getDeliveryTimes() {
        return deliveryTimes;
    }

    public void setDeliveryTimes(List<List<String>> deliveryTimes) {
        this.deliveryTimes = deliveryTimes;
    }
}
