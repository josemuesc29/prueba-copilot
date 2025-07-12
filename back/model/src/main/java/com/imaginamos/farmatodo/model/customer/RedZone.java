package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName;

public class RedZone {
    @SerializedName("cityRedZoneId")
    private String cityRedZoneId;

    @SerializedName("coordinates")
    private String coordinates;

    // Getters y setters
    public String getCityRedZoneId() {
        return cityRedZoneId;
    }

    public void setCityRedZoneId(String cityRedZoneId) {
        this.cityRedZoneId = cityRedZoneId;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }
}