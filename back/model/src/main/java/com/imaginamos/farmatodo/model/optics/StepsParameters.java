package com.imaginamos.farmatodo.model.optics;

import java.io.Serializable;
import java.util.List;

public class StepsParameters implements Serializable {

    private List<Object> powerList;

    private List<Object> cylinderList;

    private List<Object> axleList;

    private List<Object> additionList;

    private List<Object> colorList;

    public List<Object> getPowerList() {
        return powerList;
    }

    public void setPowerList(List<Object> powerList) {
        this.powerList = powerList;
    }

    public List<Object> getCylinderList() {
        return cylinderList;
    }

    public void setCylinderList(List<Object> cylinderList) {
        this.cylinderList = cylinderList;
    }

    public List<Object> getAxleList() {
        return axleList;
    }

    public void setAxleList(List<Object> axleList) {
        this.axleList = axleList;
    }

    public List<Object> getAdditionList() {
        return additionList;
    }

    public void setAdditionList(List<Object> additionList) {
        this.additionList = additionList;
    }

    public List<Object> getColorList() {
        return colorList;
    }

    public void setColorList(List<Object> colorList) {
        this.colorList = colorList;
    }

    @Override
    public String toString() {
        return "StepsParameters{" +
                "powerList=" + powerList +
                ", cylinderList=" + cylinderList +
                ", axleList=" + axleList +
                ", additionList=" + additionList +
                ", colorList=" + colorList +
                '}';
    }
}
