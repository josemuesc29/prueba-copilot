package com.imaginamos.farmatodo.networking.models.addresses;

/**
 * Created by SergioAlejandro on 4/04/2018.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class SIDataAddressPredictionRes implements StandardAddressPrediction {

    @SerializedName("estado")
    @Expose
    private String status;
    @SerializedName("barrio")
    @Expose
    private String neighborhood;
    @SerializedName("dirtrad")
    @Expose
    private String address;
    @SerializedName("latitude")
    @Expose
    private double latitude;
    @SerializedName("longitude")
    @Expose
    private double longitude;
    private String placeName = "";

    @Expose
    @SerializedName("zona1")
    private String storeName;

    @Expose
    @SerializedName("zona3")
    private String idStore;

    @Expose
    @SerializedName("deliveryType")
    private String deliveryType;

    @Expose
    @SerializedName("isMultiOrigin")
    private boolean isMultiOrigin;

    private String city;

    private String cityCode;
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

    @Override
    public String getPlaceName() {
        return placeName;
    }

    @Override
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
        return "SIDataAddressPredictionRes{" +
                ", longitude='" + longitude + '\'' +
                ", dirtrad='" + address + '\'' +
                ", latitude='" + latitude + '\'' +
                ", estado='" + status + '\'' +
                ", neighborhood='" + neighborhood + '\'' +
                ", city='" + city + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", isMultiOrigin='" + isMultiOrigin + '\'' +
                ", deliveryType='" + deliveryType + '\'' +
                '}';
    }
}
