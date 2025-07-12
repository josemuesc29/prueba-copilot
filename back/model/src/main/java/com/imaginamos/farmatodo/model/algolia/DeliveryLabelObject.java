package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class DeliveryLabelObject {

    private List<DeliveryLabelDetail> values;
    private String objectID;

    public DeliveryLabelObject() {
    }

    public DeliveryLabelObject(String objectID, List<DeliveryLabelDetail> values) {
        this.objectID = objectID;
        this.values = values;
    }

    public List<DeliveryLabelDetail> getValues() { return values; }

    public void setValues(List<DeliveryLabelDetail> values) { this.values = values; }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }
}

