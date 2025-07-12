package com.imaginamos.farmatodo.model.item;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.Objects;

public class OpticalItemFilter {
    private Double power;

    private Double powerSecondPosition;

    private Double cylinder;

    private Double cylinderSecondPosition;
    private Integer axle;

    private Integer axleSecondPosition;
    private String addition;

    private String additionSecondPosition;

    private String lensColor;

    private String lensColorSecondPosition;

    private Integer quantity;

    private Integer quantitySecondPosition;

    private EyeDirectionEnum eyeDirection;

    private EyeDirectionEnum eyeDirectionSecondPosition;

    private String mainItem;

    public OpticalItemFilter(Double power, Double powerSecondPosition, Double cylinder, Double cylinderSecondPosition, Integer axle, Integer axleSecondPosition, String addition, String additionSecondPosition, String lensColor, String lensColorSecondPosition, Integer quantity, Integer quantitySecondPosition, EyeDirectionEnum eyeDirection, EyeDirectionEnum eyeDirectionSecondPosition, String mainItem) {
        this.power = power;
        this.powerSecondPosition = powerSecondPosition;
        this.cylinder = cylinder;
        this.cylinderSecondPosition = cylinderSecondPosition;
        this.axle = axle;
        this.axleSecondPosition = axleSecondPosition;
        this.addition = addition;
        this.additionSecondPosition = additionSecondPosition;
        this.lensColor = lensColor;
        this.lensColorSecondPosition = lensColorSecondPosition;
        this.quantity = quantity;
        this.quantitySecondPosition = quantitySecondPosition;
        this.eyeDirection = eyeDirection;
        this.eyeDirectionSecondPosition = eyeDirectionSecondPosition;
        this.mainItem = mainItem;
    }

    public OpticalItemFilter() {
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        if (Objects.nonNull(power))
            this.power = power;
    }

    public Double getCylinder() {
        return cylinder;
    }

    public void setCylinder(Double cylinder) {
        if (Objects.nonNull(cylinder))
            this.cylinder = cylinder;
    }

    public Integer getAxle() {
        return axle;
    }

    public void setAxle(Integer axle) {
        if (Objects.nonNull(axle))
            this.axle = axle;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addiction) {
        if (Objects.nonNull(addiction))
            this.addition = addiction;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getLensColor() {
        return lensColor;
    }

    public void setLensColor(String lensColor) {
        if (Objects.nonNull(lensColor))
            this.lensColor = lensColor;
    }

    public EyeDirectionEnum getEyeDirection() {
        return eyeDirection;
    }

    public void setEyeDirection(EyeDirectionEnum eyeDirection) {
        this.eyeDirection = eyeDirection;
    }

    public String getMainItem() {
        return mainItem;
    }

    public void setMainItem(String mainItem) {
        this.mainItem = mainItem;
    }


    public Double getPowerSecondPosition() {
        return powerSecondPosition;
    }

    public void setPowerSecondPosition(Double powerSecondPosition) {
        if (Objects.nonNull(powerSecondPosition))
            this.powerSecondPosition = powerSecondPosition;
    }

    public Double getCylinderSecondPosition() {
        return cylinderSecondPosition;
    }

    public void setCylinderSecondPosition(Double cylinderSecondPosition) {
        if (Objects.nonNull(cylinderSecondPosition))
            this.cylinderSecondPosition = cylinderSecondPosition;
    }

    public Integer getAxleSecondPosition() {
        return axleSecondPosition;
    }

    public void setAxleSecondPosition(Integer axleSecondPosition) {
        if (Objects.nonNull(axleSecondPosition))
            this.axleSecondPosition = axleSecondPosition;
    }

    public String getAdditionSecondPosition() {
        return additionSecondPosition;
    }

    public void setAdditionSecondPosition(String additionSecondPosition) {
        if (Objects.nonNull(additionSecondPosition))
            this.additionSecondPosition = additionSecondPosition;
    }

    public String getLensColorSecondPosition() {
        return lensColorSecondPosition;
    }

    public void setLensColorSecondPosition(String lensColorSecondPosition) {
        if (Objects.nonNull(lensColorSecondPosition))
            this.lensColorSecondPosition = lensColorSecondPosition;
    }

    public Integer getQuantitySecondPosition() {
        return quantitySecondPosition;
    }

    public void setQuantitySecondPosition(Integer quantitySecondPosition) {
        this.quantitySecondPosition = quantitySecondPosition;
    }

    public EyeDirectionEnum getEyeDirectionSecondPosition() {
        return eyeDirectionSecondPosition;
    }

    public void setEyeDirectionSecondPosition(EyeDirectionEnum eyeDirectionSecondPosition) {
        this.eyeDirectionSecondPosition = eyeDirectionSecondPosition;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
