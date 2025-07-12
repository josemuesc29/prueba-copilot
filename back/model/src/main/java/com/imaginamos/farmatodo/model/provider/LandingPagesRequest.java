package com.imaginamos.farmatodo.model.provider;

import com.imaginamos.farmatodo.model.dto.EnableForEnum;

import java.util.Map;

public class LandingPagesRequest {
    private String idCustomerWebSafe;
    private String token;
    private String tokenIdWebSafe;
    private EnableForEnum source;
    private int carrouselLimit;
    private int idStoreGroup;
    private String provider;

    private Map<String, Object> talonOneData;

    public Map<String, Object> getTalonOneData() {
        return talonOneData;
    }

    public void setTalonOneData(Map<String, Object> talonOneData) {
        this.talonOneData = talonOneData;
    }

    public String getIdCustomerWebSafe() {
        return idCustomerWebSafe;
    }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) {
        this.idCustomerWebSafe = idCustomerWebSafe;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public EnableForEnum getSource() {
        return source;
    }

    public void setSource(EnableForEnum source) {
        this.source = source;
    }

    public int getCarrouselLimit() {
        return carrouselLimit;
    }

    public void setCarrouselLimit(int carrouselLimit) {
        this.carrouselLimit = carrouselLimit;
    }

    public int getIdStoreGroup() {
        return idStoreGroup;
    }

    public void setIdStoreGroup(int idStoreGroup) {
        this.idStoreGroup = idStoreGroup;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "LandingPagesRequest{" +
                "idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
                ", token='" + token + '\'' +
                ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
                ", source=" + source +
                ", carrouselLimit=" + carrouselLimit +
                ", idStoreGroup=" + idStoreGroup +
                ", provider='" + provider + '\'' +
                '}';
    }
}
