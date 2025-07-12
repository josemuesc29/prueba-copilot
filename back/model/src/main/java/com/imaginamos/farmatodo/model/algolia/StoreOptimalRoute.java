package com.imaginamos.farmatodo.model.algolia;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class StoreOptimalRoute {

    private List<Long> enable = new ArrayList<>();
    private List<Long> disable = new ArrayList<>();

    public List<Long> getEnable() {return enable;}

    public void setEnable(List<Long> enable) {this.enable = enable;}

    public List<Long> getDisable() {return disable;}

    public void setDisable(List<Long> disable) {this.disable = disable;}

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
