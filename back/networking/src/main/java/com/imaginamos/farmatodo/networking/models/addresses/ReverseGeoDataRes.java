package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SergioRojas on 28/05/18.
 */

public class ReverseGeoDataRes implements StandardAddressPrediction {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("neighborhood1")
    @Expose
    private String neighborhood;
    @SerializedName("address1")
    @Expose
    private String address;

    @Expose
    @SerializedName("zona1")
    private String storeName;

    @Expose
    @SerializedName("zona3")
    private String idStore;

    @Expose
    @SerializedName("city1")
    private String city;

    @Expose
    @SerializedName("deliveryType")
    private String deliveryType;

    @Expose
    @SerializedName("isMultiOrigin")
    private boolean isMultiOrigin;

    private String placeName = "";

    private String cityCode;

    private double latitude;

    private double longitude;

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

    @Override
    public String getPlaceName() {
        return placeName;
    }

    @Override
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public boolean getIsMultiOrigin() {
        return isMultiOrigin;
    }

    public void setIsMultiOrigin(boolean isMultiOrigin) {
        this.isMultiOrigin = isMultiOrigin;
    }

    @Override
    public String toString() {
        return "ReverseGeoDataRes{" +
                "status='" + status + '\'' +
                ", neighborhood='" + neighborhood + '\'' +
                ", address='" + address + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", IsMultiOrigin=" + isMultiOrigin +
                ", deliveryType=" + deliveryType +
                ", city=" + city +
                ", idStore=" + idStore +
                '}';
    }
}
