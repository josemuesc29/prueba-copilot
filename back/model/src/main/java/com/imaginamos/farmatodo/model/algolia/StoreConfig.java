package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 1/11/2018.
 */
public class StoreConfig {

    private Integer store;
    private String  day;
    private Integer open;
    private Integer close;
    private String  objectID;

    public Integer getStore() {
        return store;
    }

    public void setStore(Integer store) {
        this.store = store;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getOpen() {
        return open;
    }

    public void setOpen(Integer open) {
        this.open = open;
    }

    public Integer getClose() {
        return close;
    }

    public void setClose(Integer close) {
        this.close = close;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    @Override
    public String toString() {
        return "StoreConfig{" +
                "store=" + store +
                ", day='" + day + '\'' +
                ", open=" + open +
                ", close=" + close +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}
