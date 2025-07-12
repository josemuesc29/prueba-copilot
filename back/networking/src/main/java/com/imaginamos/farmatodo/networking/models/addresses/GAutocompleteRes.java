package com.imaginamos.farmatodo.networking.models.addresses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
public class GAutocompleteRes {

    @SerializedName("predictions")
    @Expose
    private List<GPlacePredictionRes> predictions = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<GPlacePredictionRes> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<GPlacePredictionRes> predictions) {
        this.predictions = predictions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
