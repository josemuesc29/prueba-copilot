package com.imaginamos.farmatodo.networking.models.addresses;

import java.util.ArrayList;
import java.util.List;

public class AddressPredictionRes {

    private List<StandardAddressPrediction> addressPredictions = new ArrayList<>();
    private String input;
    private String city;
    private String nextPlaceId = "";


    public List<StandardAddressPrediction> getAddressPredictions() {
        return addressPredictions;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isSuccessful() {
        return !addressPredictions.isEmpty();
    }

    public void setAddressPredictions(List<StandardAddressPrediction> addressPredictions) {
        this.addressPredictions = addressPredictions;
    }

    public String getNextPlaceId() {
        return nextPlaceId;
    }

    public void setNextPlaceId(String nextPlaceId) {
        this.nextPlaceId = nextPlaceId;
    }
}
