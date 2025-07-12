package com.imaginamos.farmatodo.model.optics;

import java.io.Serializable;
import java.util.Objects;

public class ParametersOptics implements Serializable {
    private Double power;
    private Double cylinder;
    private Integer axle;
    private String addition;

    private String lensColor;


    public Double getPower() {
        return power;
    }

    public void setPowerNonNull(Double power) {
        if (Objects.nonNull(power))
            this.power = power;
    }

    public Double getCylinder() {
        return cylinder;
    }

    public void setCylinderNonNull(Double cylinder) {
        if (Objects.nonNull(cylinder))
            this.cylinder = cylinder;
    }

    public Integer getAxle() {
        return axle;
    }

    public void setAxleNonNull(Integer axle) {
        if (Objects.nonNull(axle))
            this.axle = axle;
    }

    public String getAddiction() {
        return addition;
    }

    public void setAddictionNonNull(String addiction) {
        if (Objects.nonNull(addiction))
            this.addition = addiction;
    }

    public String getLensColor() {
        return lensColor;
    }

    public void setLensColorNonNull(String lensColor) {
        if (Objects.nonNull(cylinder))
            this.lensColor = lensColor;
    }

    public ParametersOptics() {
        this.power = 0D;
        this.cylinder = 0D;
        this.axle = 0;
        this.addition = "";
        this.lensColor = "";
    }

    @Override
    public String toString() {
        return "ParametersOptics{" +
                "power=" + power +
                ", cylinder=" + cylinder +
                ", axle=" + axle +
                ", addiction=" + addition +
                ", color='" + lensColor + '\'' +
                '}';
    }
}
