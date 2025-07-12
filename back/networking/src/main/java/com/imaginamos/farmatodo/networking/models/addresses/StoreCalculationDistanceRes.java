package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class StoreCalculationDistanceRes {

    private int store;
    private Double distanceInKms;
    private float latStore;
    private float lngStore;

    public int getStore() {
        return store;
    }

    public void setStore(int store) {
        this.store = store;
    }

    public Double getDistanceInKms() {
        return distanceInKms;
    }

    public float getLatStore() {
        return latStore;
    }

    public void setLatStore(float latStore) {
        this.latStore = latStore;
    }

    public float getLngStore() {
        return lngStore;
    }

    public void setLngStore(float lngStore) {
        this.lngStore = lngStore;
    }

    public void setDistanceInKms(Double distanceInKms) {
        this.distanceInKms = distanceInKms;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
