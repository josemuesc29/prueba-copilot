package com.imaginamos.farmatodo.model.algolia;


public class PaymentMethodsAlgoliaConfig {
    private Boolean cash;
    private Boolean onLine;
    private Boolean dataphone;
    private Boolean pse;

    public Boolean getCash() {
        return cash;
    }

    public void setCash(Boolean cash) {
        this.cash = cash;
    }

    public Boolean getOnLine() {
        return onLine;
    }

    public void setOnLine(Boolean onLine) {
        this.onLine = onLine;
    }

    public Boolean getDataphone() {
        return dataphone;
    }

    public void setDataphone(Boolean dataphone) {
        this.dataphone = dataphone;
    }

    public Boolean getPse() {
        return pse;
    }

    public void setPse(Boolean pse) {
        this.pse = pse;
    }


    @Override
    public String toString() {
        return "PaymentMethodsAlgoliaConfig{" +
                "cash=" + cash +
                ", onLine=" + onLine +
                ", dataphone=" + dataphone +
                ", pse=" + pse +
                '}';
    }
}
