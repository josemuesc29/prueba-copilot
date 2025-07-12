package com.imaginamos.farmatodo.model.algolia;


public class PaymentMethodsAlgoliaConfigV2 {

    private String source;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    @Override
    public String toString() {
        return "PaymentMethodsAlgoliaConfig{" +
                "source=" + source +
                "cash=" + cash +
                ", onLine=" + onLine +
                ", dataphone=" + dataphone +
                ", pse=" + pse +
                '}';
    }
}
