package com.imaginamos.farmatodo.networking.models.addresses;

import java.util.ArrayList;

public class AutocompleteLupapRes {
    private ArrayList<Prediction> predictions;

    public ArrayList<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(ArrayList<Prediction> predictions) {
        this.predictions = predictions;
    }
}
