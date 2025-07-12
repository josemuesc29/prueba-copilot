package com.imaginamos.farmatodo.model.algolia.eta;


public class Variable {

    private String key;
    private Integer additionalTimeInMinutes;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getAdditionalTimeInMinutes() {
        return additionalTimeInMinutes;
    }

    public void setAdditionalTimeInMinutes(Integer additionalTimeInMinutes) {
        this.additionalTimeInMinutes = additionalTimeInMinutes;
    }
}
