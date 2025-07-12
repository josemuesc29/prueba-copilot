package com.imaginamos.farmatodo.model.algolia;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;

public class OpticalProduct {
    private Long id;
    private OpticalItemFilter parameters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OpticalItemFilter getParameters() {
        return parameters;
    }

    public void setParameters(OpticalItemFilter parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
