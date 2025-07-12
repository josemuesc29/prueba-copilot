package com.imaginamos.farmatodo.networking.talonone.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

public class TrackEventItemPurchasedRequest {
    private String profileId;
    private String type;
    private List<LinkedTreeMap<String, Object>> attributes;

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

    public List<LinkedTreeMap<String, Object>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<LinkedTreeMap<String, Object>> attributes) {
        this.attributes = attributes;
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
