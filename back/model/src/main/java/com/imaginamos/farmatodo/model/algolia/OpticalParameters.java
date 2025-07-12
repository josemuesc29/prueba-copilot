package com.imaginamos.farmatodo.model.algolia;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class OpticalParameters {
    private Double power;
    private Integer axle;
    private Double cylinder;
    private String addition;
    private String lensColor;

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Integer getAxle() {
        return axle;
    }

    public void setAxle(Integer axle) {
        this.axle = axle;
    }

    public Double getCylinder() {
        return cylinder;
    }

    public void setCylinder(Double cylinder) {
        this.cylinder = cylinder;
    }

    public String getLensColor() {
        return lensColor;
    }

    public void setLensColor(String lensColor) {
        this.lensColor = lensColor;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
