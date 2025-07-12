package com.imaginamos.farmatodo.model.algolia.eta;

import java.util.List;

public class VariableByCity {

    private String cityId;
    private List<String> variablesToApply;

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public List<String> getVariablesToApply() {
        return variablesToApply;
    }

    public void setVariablesToApply(List<String> variablesToApply) {
        this.variablesToApply = variablesToApply;
    }
}
