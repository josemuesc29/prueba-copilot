package com.imaginamos.farmatodo.model.braze;

import java.util.List;

public class BrazeEventCreate {
    private String userId;
    private List<BrazeProperties> itemsData;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<BrazeProperties> getItemsData() {
        return itemsData;
    }

    public void setItemsData(List<BrazeProperties> itemsData) {
        this.itemsData = itemsData;
    }
}
