package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GPlacePredictionRes {

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("place_id")
    @Expose
    private String placeId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public String toString() {
        return "GPlacePredictionRes{" +
                "description='" + description + '\'' +
                ", id='" + id + '\'' +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}
