package com.imaginamos.farmatodo.model.algolia;

public class DeliveryTimeLabelGeneric {

    public DeliveryTimeLabelGeneric() {
    }

    public DeliveryTimeLabelGeneric(String express, String national, String envialoYa) {
        this.express = express;
        this.national = national;
        this.envialoYa = envialoYa;
    }

    private String express;
    private String national;
    private String envialoYa;

    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
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
