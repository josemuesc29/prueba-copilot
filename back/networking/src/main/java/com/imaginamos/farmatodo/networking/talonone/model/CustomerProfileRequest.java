package com.imaginamos.farmatodo.networking.talonone.model;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class CustomerProfileRequest {
    private Boolean runRuleEngine;
    private Boolean dry;
    private LinkedTreeMap<String, String> attributes;

    public Boolean getRunRuleEngine() {
        return runRuleEngine;
    }

    public void setRunRuleEngine(Boolean runRuleEngine) {
        this.runRuleEngine = runRuleEngine;
    }

    public Boolean getDry() {
        return dry;
    }

    public void setDry(Boolean dry) {
        this.dry = dry;
    }

    public LinkedTreeMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(LinkedTreeMap<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
