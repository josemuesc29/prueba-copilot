package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 17/10/2018.
 */
public class RecommendedItem {

    private String objectID;
    private Integer itemId;
    private Integer store;

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getStore() {
        return store;
    }

    public void setStore(Integer store) {
        this.store = store;
    }

}
