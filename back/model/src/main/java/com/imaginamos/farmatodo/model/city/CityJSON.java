package com.imaginamos.farmatodo.model.city;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CityJSON {

    private String id;
    private String name;
    private String geoCityCode;
    private String country;
    private String phone;
    private Integer status;
    @JsonProperty("store")
    private DefaultStoreResponse defaultStore;
    private String deliveryType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGeoCityCode() {
        return geoCityCode;
    }

    public void setGeoCityCode(String geoCityCode) {
        this.geoCityCode = geoCityCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public DefaultStoreResponse getDefaultStore() {
        return defaultStore;
    }

    public void setDefaultStore(DefaultStoreResponse defaultStore) {
        this.defaultStore = defaultStore;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }
}