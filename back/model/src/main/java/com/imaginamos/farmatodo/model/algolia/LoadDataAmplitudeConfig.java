package com.imaginamos.farmatodo.model.algolia;

public class LoadDataAmplitudeConfig {

    private boolean active;
    private boolean orderCompleted;
    private String objectID;

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }

    public boolean isOrderCompleted() { return orderCompleted; }

    public void setOrderCompleted(boolean orderCompleted) { this.orderCompleted = orderCompleted; }
}
