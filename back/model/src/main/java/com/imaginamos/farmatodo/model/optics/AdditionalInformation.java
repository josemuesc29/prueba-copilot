package com.imaginamos.farmatodo.model.optics;

import java.io.Serializable;

public class AdditionalInformation implements Serializable {

    private AdditionalInformationType type;

    private String name;

    private String value;

    private String path;

    public AdditionalInformationType getType() {
        return type;
    }

    public void setType(AdditionalInformationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
