package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 10/03/2018.
 */
public class Item {

    private String id;
    private String objectID;
    private String classification;

    public Item(){}

    public Item(String itemID,String storeID,String classification){
        this.id = itemID;
        this.objectID = itemID+storeID;
        this.classification = classification;
    }

    public String getItemID() {
        return this.id;
    }

    public Item setItemID(String itemID) {
        this.id = itemID;
        return this;
    }

    public String getObjectID() {
        return this.objectID;
    }

    public Item setObjectID(String objectID) {
        this.objectID = objectID;
        return this;
    }

    public String getClassification() {
        return this.classification;
    }

    public Item setClassification(String classification) {
        this.classification = classification;
        return this;
    }

}
