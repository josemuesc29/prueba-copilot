package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class Filter {

    private String type;
    private String name;
    private List<FilterDetail> values;
    private String objectID;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FilterDetail> getValues() {
        return values;
    }

    public void setValues(List<FilterDetail> values) {
        this.values = values;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", values=" + values +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}
