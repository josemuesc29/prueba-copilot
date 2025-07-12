package com.imaginamos.farmatodo.networking.models.addresses;

public class ValidateGeoZoneReq {

    private String cityId;

    private String deliveryType;

    private double addressLat;

    private double addressLng;

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public double getAddressLat() {
        return addressLat;
    }

    public void setAddressLat(double addressLat) {
        this.addressLat = addressLat;
    }

    public double getAddressLng() {
        return addressLng;
    }

    public void setAddressLng(double addressLng) {
        this.addressLng = addressLng;
    }

    @Override
    public String toString() {
        return "ValidateGeoZoneReq{" +
                "cityId='" + cityId + '\'' +
                ", deliveryType='" + deliveryType + '\'' +
                ", addressLat=" + addressLat +
                ", addressLng=" + addressLng +
                '}';
    }
}
