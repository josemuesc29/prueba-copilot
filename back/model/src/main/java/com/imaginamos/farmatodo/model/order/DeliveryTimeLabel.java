package com.imaginamos.farmatodo.model.order;

public class DeliveryTimeLabel {

    public DeliveryTimeLabel() {}

    private String label;
    private String express;
    private String provider;
    private String national;
    private String envialoYa;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

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
