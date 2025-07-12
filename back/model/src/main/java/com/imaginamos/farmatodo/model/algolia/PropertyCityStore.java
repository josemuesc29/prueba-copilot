package com.imaginamos.farmatodo.model.algolia;

public class PropertyCityStore {

    private String id;
    private String defaultStore;
    private String deliveryType;
    private String geoCityCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultStore() {
        return defaultStore;
    }

    public void setDefaultStore(String defaultStore) {
        this.defaultStore = defaultStore;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getGeoCityCode() {
        return geoCityCode;
    }

    public void setGeoCityCode(String geoCityCode) {
        this.geoCityCode = geoCityCode;
    }
}
