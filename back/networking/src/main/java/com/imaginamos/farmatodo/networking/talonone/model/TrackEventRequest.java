package com.imaginamos.farmatodo.networking.talonone.model;

import com.google.gson.internal.LinkedTreeMap;

public class TrackEventRequest {
    private String profileId;
    private String type;
    private LinkedTreeMap<String, Object> attributes;

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LinkedTreeMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(LinkedTreeMap<String, Object> attributes) {
        this.attributes = attributes;
    }
}
