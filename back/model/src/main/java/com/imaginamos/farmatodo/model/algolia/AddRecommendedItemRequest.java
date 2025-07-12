package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 18/10/2018.
 */
public class AddRecommendedItemRequest {

    private Integer itemId;
    private Integer store;

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
