package com.imaginamos.farmatodo.model.algolia;

public class PropertyStoreById {
    private int defaultStore;
    private int  idStore;
    private String name;
    private String storeCity;

    public PropertyStoreById() {
    }

    public int getDefaultStore() {
        return defaultStore;
    }

    public void setDefaultStore(int defaultStore) {
        this.defaultStore = defaultStore;
    }

    public int getIdStore() {
        return idStore;
    }

    public void setIdStore(int idStore) {
        this.idStore = idStore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoreCity() {
        return storeCity;
    }

    public void setStoreCity(String storeCity) {
        this.storeCity = storeCity;
    }
}
