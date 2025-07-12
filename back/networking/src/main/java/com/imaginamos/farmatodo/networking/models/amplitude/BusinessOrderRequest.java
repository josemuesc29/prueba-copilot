package com.imaginamos.farmatodo.networking.models.amplitude;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.List;

public class BusinessOrderRequest {

    private List<String> items;

    public List<String> getItems() { return items; }

    public void setItems(List<String> items) { this.items = items; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
