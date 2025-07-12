package com.imaginamos.farmatodo.model.algolia;

import java.io.Serializable;

/**
 * Created by JPuentes on 1/11/2018.
 */
public class Holiday implements Serializable {

    private String date;
    private String objectID;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
