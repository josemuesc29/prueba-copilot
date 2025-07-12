package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class StoreValidWhitCordinatesReq {

    private float latitude;
    private float longitude;

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
