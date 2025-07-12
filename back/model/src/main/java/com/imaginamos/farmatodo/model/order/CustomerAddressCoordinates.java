package com.imaginamos.farmatodo.model.order;

/**
 * Created by jhon.puentes@farmatodo.com
 * 2019-12-16
 */
public class CustomerAddressCoordinates {

    private int addressId;
    private double longitude;
    private double latitude;

    public CustomerAddressCoordinates(int addressId, double longitude, double latitude) {
        this.addressId = addressId;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getAddressId() {
        return addressId;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
