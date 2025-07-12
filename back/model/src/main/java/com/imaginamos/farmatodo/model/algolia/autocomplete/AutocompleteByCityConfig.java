package com.imaginamos.farmatodo.model.algolia.autocomplete;

import java.util.List;

/**
 *
 * Configuration in Algolia
 * objectID: AUTOCOMPLETE.BY.CITY.CONFIG
 *
 * */
public class AutocompleteByCityConfig {

    private Boolean useFastAutocomplete;
    private List<City> cities;
    private String objectID;

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public Boolean getUseFastAutocomplete() {
        return useFastAutocomplete;
    }

    public void setUseFastAutocomplete(Boolean useFastAutocomplete) {
        this.useFastAutocomplete = useFastAutocomplete;
    }

    @Override
    public String toString() {
        return "AutocompleteByCityConfig{" +
                "cities=" + this.cities.size() +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}
