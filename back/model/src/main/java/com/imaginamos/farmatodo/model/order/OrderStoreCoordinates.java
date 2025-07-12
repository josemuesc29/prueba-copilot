package com.imaginamos.farmatodo.model.order;

/**
 * Created by jhon.puentes@farmatodo.com
 * 2019-12-16
 */
public class OrderStoreCoordinates {

    private int storeId;
    private double longitude;
    private double latitude;

    public OrderStoreCoordinates(int storeId, double longitude, double latitude) {
        this.storeId = storeId;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getStoreId() {
        return storeId;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
