package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GGeoReverseRes {

    @SerializedName("results")
    @Expose
    private List<Result> results;

    private String address = "";

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getAddress() {
        if (results.isEmpty()) return "";
        return results.get(0).getAddress();
    }

    private class Result {

        @SerializedName("formatted_address")
        @Expose
        private String formattedAddress = "";

        String getAddress() {
            if (!formattedAddress.isEmpty() && formattedAddress.contains(",")) {
                String[] splits = formattedAddress.split(",");
                if (splits.length > 1)
                    return splits[0];
            }
            return "";
        }

        String getFormatedAddress() {
            return formattedAddress;
        }

        void setFormatedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

    }

}
