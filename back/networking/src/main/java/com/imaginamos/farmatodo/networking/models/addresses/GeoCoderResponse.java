package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SergioAlejandro on 4/04/2018.
 */

public class GeoCoderResponse {

    @SerializedName("data")
    @Expose
    private SIDataAddressPredictionRes data;

    public SIDataAddressPredictionRes getData() {
        return data;
    }

    public void setData(SIDataAddressPredictionRes data) {
        this.data = data;
    }

    @Override
    public String toString() {
        Gson g = new Gson();
        return g.toJson(this);
    }
}
