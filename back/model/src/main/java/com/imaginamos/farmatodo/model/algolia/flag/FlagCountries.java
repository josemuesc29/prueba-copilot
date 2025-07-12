package com.imaginamos.farmatodo.model.algolia.flag;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class FlagCountries {

    private String  id;
    private String  countryName;
    private int     prefix;
    private String  flagUrl;
    private boolean active;

    public FlagCountries() {
    }

    public FlagCountries(String id, String countryName, int prefix, String flagUrl, boolean active) {
        this.id = id;
        this.countryName = countryName;
        this.prefix = prefix;
        this.flagUrl = flagUrl;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public int getPrefix() {
        return prefix;
    }

    public void setPrefix(int prefix) {
        this.prefix = prefix;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}