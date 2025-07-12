package com.imaginamos.farmatodo.model.algolia;

public class DeliveryTimeLabelCart {

    public DeliveryTimeLabelCart() {
    }

    public DeliveryTimeLabelCart(String express, String provider, String national, String envialoYa) {
        this.express = express;
        this.provider = provider;
        this.national = national;
        this.envialoYa = envialoYa;
    }

    private String express;
    private String provider;
    private String national;
    private String envialoYa;

    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getNational() {
        return national;
    }

    public void setNational(String national) {
        this.national = national;
    }

    public String getEnvialoYa() {
        return envialoYa;
    }

    public void setEnvialoYa(String envialoYa) {
        this.envialoYa = envialoYa;
    }
}
