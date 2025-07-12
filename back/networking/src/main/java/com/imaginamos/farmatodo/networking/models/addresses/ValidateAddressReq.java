package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class ValidateAddressReq {
    private Long idCustomer;
    private String city;
    private String address;
    private String placeId;
    private String tokenIdWebSafe;
    private Double lat;
    private Double lng;
    private String token;
    private Boolean isInsideGeomalla;

    public ValidateAddressReq() {
    }

    public Long getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(Long idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public boolean getIsInsideGeomalla() {
        return isInsideGeomalla;
    }

    public void setIsInsideGeomalla(Boolean isInsideGeomalla) {
        this.isInsideGeomalla = isInsideGeomalla;
    }

    public boolean isValid(){
        return token != null && !token.isEmpty() && tokenIdWebSafe != null && !tokenIdWebSafe.isEmpty() && city != null && !city.isEmpty();
    }

    @Override
    public String toString() {
        Gson g = new Gson();
        return g.toJson(this);
    }
}
