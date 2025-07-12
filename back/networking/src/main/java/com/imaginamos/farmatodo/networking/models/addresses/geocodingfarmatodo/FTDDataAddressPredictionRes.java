package com.imaginamos.farmatodo.networking.models.addresses.geocodingfarmatodo;

public class FTDDataAddressPredictionRes {

    private String status;
    private String neighborhood;
    private String address;
    private double latitude;
    private double longitude;
    private String placeName = "";
    private String storeName;
    private String idStore;

    public FTDDataAddressPredictionRes() { }

    public FTDDataAddressPredictionRes(String status, String neighborhood, String address,
                                       double latitude, double longitude, String placeName,
                                       String storeName, String idStore) {
        this.status = status;
        this.neighborhood = neighborhood;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.storeName = storeName;
        this.idStore = idStore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getIdStore() {
        return idStore;
    }

    public void setIdStore(String idStore) {
        this.idStore = idStore;
    }
}
