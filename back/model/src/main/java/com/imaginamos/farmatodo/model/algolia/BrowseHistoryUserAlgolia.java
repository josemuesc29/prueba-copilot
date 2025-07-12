package com.imaginamos.farmatodo.model.algolia;

import java.io.Serializable;
import java.util.List;

public class BrowseHistoryUserAlgolia implements Serializable {

    private List<String> items;
    private String objectID;


    public BrowseHistoryUserAlgolia() {
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    @Override
    public String toString() {
        return "BrowseHistoryUserAlgolia {" +
                "items='" + items.toString() + '\'' +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}

