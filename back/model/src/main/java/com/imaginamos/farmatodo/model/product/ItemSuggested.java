package com.imaginamos.farmatodo.model.product;

import java.util.List;

public class ItemSuggested {

    private String objectID;
    private List<String> id_suggested;

    public ItemSuggested() {}

    public ItemSuggested(String objectID, List<String> id_suggested) {
        this.objectID = objectID;
        this.id_suggested = id_suggested;
    }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }

    public List<String> getId_suggested() { return id_suggested; }

    public void setId_suggested(List<String> id_suggested) { this.id_suggested = id_suggested; }
}
