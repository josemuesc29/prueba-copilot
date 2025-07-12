package com.imaginamos.farmatodo.networking.models.addresses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SergioRojas on 28/05/18.
 */

public class ReverseGeoReq {

    @SerializedName("longitude")
    @Expose
    private double longitude;

    @SerializedName("latitude")
    @Expose
    private double latitude;

    @SerializedName("isInsideGeomalla")
    @Expose
    private boolean isInsideGeomalla;

    private String city;
    private String country;
    private String deliveryType;


    public ReverseGeoReq() {
    }

    public ReverseGeoReq(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public ReverseGeoReq(double latitude, double longitude, boolean isInsideGeomalla) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.isInsideGeomalla = isInsideGeomalla;
    }

    public ReverseGeoReq(double latitude, double longitude, boolean isInsideGeomalla, String city, String deliveryType) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.isInsideGeomalla = isInsideGeomalla;
        this.city = city;
        this.deliveryType = deliveryType;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean getIsInsideGeomalla() { return isInsideGeomalla; }

    public void setIsInsideGeomalla(boolean isInsideGeomalla) {
        this.isInsideGeomalla = isInsideGeomalla;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    @Override
    public String toString() {
        return "ReverseGeoReq{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", isInsideGeomalla=" + isInsideGeomalla +
                '}';
    }
    public String toStringJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
