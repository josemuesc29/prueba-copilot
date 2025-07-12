package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class CartDeliveryLabelConfig {

    private String objectID;

    private List<CartDeliveryLabelConfigValue> values;

    public List<CartDeliveryLabelConfigValue> getValues() { return values; }

    public void setValues(List<CartDeliveryLabelConfigValue> values) {
        this.values = values;
    }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }
}
