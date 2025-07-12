package com.imaginamos.farmatodo.model.provider;

import java.util.List;

public class ComponentProvider {
    private String componentType;
    private List<String>  enableFor;
    private DataFrom dataFrom;
    private boolean active;
    private String label;
    private int position;

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public List<String> getEnableFor() {
        return enableFor;
    }

    public void setEnableFor(List<String> enableFor) {
        this.enableFor = enableFor;
    }

    public DataFrom getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(DataFrom dataFrom) {
        this.dataFrom = dataFrom;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "ComponentProvider{" +
                "componentType='" + componentType + '\'' +
                ", enableFor=" + enableFor +
                ", dataFrom=" + dataFrom +
                ", active=" + active +
                ", label='" + label + '\'' +
                ", position=" + position +
                '}';
    }
}
