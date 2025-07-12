package com.imaginamos.farmatodo.model.algolia;

public class OpticalProductParameters {

    private Long id;
    private Double power;
    private Double cylinder;
    private Integer axle;
    private String addition;
    private String lensColor;

    private Long fatherId;

    private boolean isChild;


    public OpticalProductParameters() {
    }

    public OpticalProductParameters(Long id, Double power, Double cylinder, Integer axle, String addition, String lensColor, Long fatherId, boolean isChild) {
        this.id = id;
        this.power = power;
        this.cylinder = cylinder;
        this.axle = axle;
        this.addition = addition;
        this.lensColor = lensColor;
        this.fatherId = fatherId;
        this.isChild = isChild;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Double getCylinder() {
        return cylinder;
    }

    public void setCylinder(Double cylinder) {
        this.cylinder = cylinder;
    }

    public Integer getAxle() {
        return axle;
    }

    public void setAxle(Integer axle) {
        this.axle = axle;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }

    public String getLensColor() {
        return lensColor;
    }

    public void setLensColor(String lensColor) {
        this.lensColor = lensColor;
    }

    public Long getFatherId() {
        return fatherId;
    }

    public void setFatherId(Long fatherId) {
        this.fatherId = fatherId;
    }

    public boolean getChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    @Override
    public String toString() {
        return "OpticalProducts{" +
                "id=" + id +
                ", power=" + power +
                ", cylinder=" + cylinder +
                ", axle=" + axle +
                ", addition='" + addition + '\'' +
                ", lensColor='" + lensColor + '\'' +
                ", fatherId=" + fatherId +
                ", isChild=" + isChild +
                '}';
    }
}
