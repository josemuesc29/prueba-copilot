package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.List;

public class Suggesteds {
    private List<SuggestedObject>suggestsList;

    public Suggesteds() {
    }

    public Suggesteds(List<SuggestedObject> suggestsList) {
        this.suggestsList = suggestsList;
    }

    public List<SuggestedObject> getSuggestsList() {
        return suggestsList;
    }

    public void setSuggestsList(List<SuggestedObject> suggestsList) {
        this.suggestsList = suggestsList;
    }

    @Override
    public String toString() {
        Gson g = new Gson();
        return g.toJson(this);
    }
}
