package com.imaginamos.farmatodo.networking.talonone.model;

public class BagItem {
    private String id;
    private String objectID;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public BagItem() {
    }

    public BagItem(String id, String objectID) {
        this.id = id;
        this.objectID = objectID;
    }
}
