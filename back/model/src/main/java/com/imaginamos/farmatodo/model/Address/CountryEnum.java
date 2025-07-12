package com.imaginamos.farmatodo.model.Address;

public enum CountryEnum {
    COL("co"),
    VEN("ve"),
    ARG("Argentina");

    private String id;

    CountryEnum(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
