package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by SergioRojas on 28/05/18.
 */

public class ReverseGeoRes  {

    @SerializedName("data")
    @Expose
    private ReverseGeoDataRes data;

    public ReverseGeoDataRes getData() {
        return data;
    }

    public void setData(ReverseGeoDataRes data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ReverseGeoRes{" +
                "data=" + data +
                '}';
    }
}
