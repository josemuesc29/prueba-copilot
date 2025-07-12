package com.imaginamos.farmatodo.networking.models.addresses.geocodingfarmatodo;
/**
 * Representacion logica de https://www.algolia.com/apps/VCOJEYD2PO/explorer/browse/data_geocoding
 * */
public class AddressAlgolia {

    private String status;
    private String address;
    private String geoAddress;
    private String cityCode;
    private String city;
    private String idStore;
    private String storeName;
    private String latitude;
    private String longitude;
    private String country;
    private String deliveryType;
    private String objectID;

    public AddressAlgolia() {}

    public AddressAlgolia(String status, String address, String geoAddress, String cityCode, String city,
                          String idStore, String storeName, String latitude, String longitude, String country, String deliveryType,
                          String objectID) {
        this.status = status;
        this.address = address;
        this.geoAddress = geoAddress;
        this.cityCode = cityCode;
        this.city = city;
        this.idStore = idStore;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.deliveryType = deliveryType;
        this.objectID = objectID;
        this.storeName = storeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGeoAddress() {
        return geoAddress;
    }

    public void setGeoAddress(String geoAddress) {
        this.geoAddress = geoAddress;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIdStore() {
        return idStore;
    }

    public void setIdStore(String idStore) {
        this.idStore = idStore;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
