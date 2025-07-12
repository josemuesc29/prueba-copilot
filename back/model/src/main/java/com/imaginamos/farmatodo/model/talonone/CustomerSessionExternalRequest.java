package com.imaginamos.farmatodo.model.talonone;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;

import java.util.List;

public class CustomerSessionExternalRequest {
    private String profileId;
    private List<ItemAlgolia> items;

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public List<ItemAlgolia> getItems() {
        return items;
    }

    public void setItems(List<ItemAlgolia> items) {
        this.items = items;
    }
}
