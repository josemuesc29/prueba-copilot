package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActiveRedZoneData {
    @SerializedName("redZoneMessage")
    private String redZoneMessage;

    @SerializedName("redZones")
    private List<RedZone> redZones;

    // Getters y setters
    public String getRedZoneMessage() {
        return redZoneMessage;
    }

    public void setRedZoneMessage(String redZoneMessage) {
        this.redZoneMessage = redZoneMessage;
    }

    public List<RedZone> getRedZones() {
        return redZones;
    }

    public void setRedZones(List<RedZone> redZones) {
        this.redZones = redZones;
    }
}