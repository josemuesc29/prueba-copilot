package com.imaginamos.farmatodo.model.algolia;


import java.util.List;

public class PropertiesAlgoliaFacets {
    private List<String> searchFilters;
    private List<String> worldOfferFilters;

    private List<String> searchFiltersSS;

    public List<String> getSearchFilters() {
        return searchFilters;
    }

    public void setSearchFilters(List<String> searchFilters) {
        this.searchFilters = searchFilters;
    }

    public List<String> getWorldOfferFilters() {
        return worldOfferFilters;
    }

    public void setWorldOfferFilters(List<String> worldOfferFilters) {
        this.worldOfferFilters = worldOfferFilters;
    }

    public List<String> getSearchFiltersSS() {
        return searchFiltersSS;
    }

    public void setSearchFiltersSS(List<String> searchFiltersSS) {
        this.searchFiltersSS = searchFiltersSS;
    }
}
