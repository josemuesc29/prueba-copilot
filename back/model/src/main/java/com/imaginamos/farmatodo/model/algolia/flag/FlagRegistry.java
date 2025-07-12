package com.imaginamos.farmatodo.model.algolia.flag;

import java.util.List;

public class FlagRegistry {

    private List<FlagCountries> countries;
    private String objectID;

    public FlagRegistry() {
    }

    public FlagRegistry(List<FlagCountries> countries, String objectID) {
        this.countries = countries;
        this.objectID = objectID;
    }

    public List<FlagCountries> getCountries() {
        return countries;
    }

    public void setCountries(List<FlagCountries> countries) {
        this.countries = countries;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}