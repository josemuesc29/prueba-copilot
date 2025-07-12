package com.imaginamos.farmatodo.model.algolia;


import java.util.List;

public class PaymentMethodsV2 {

    private String source;
    List<String> enable_payments;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getEnable_payments() {
        return enable_payments;
    }

    public void setEnable_payments(List<String> enable_payments) {
        this.enable_payments = enable_payments;
    }

    @Override
    public String toString() {
        return "PaymentMethodsAlgoliaConfig{" +
                "source=" + source +
                "cash=" +
                ", onLine=" +
                ", dataphone=" +
                ", pse=" +
                '}';
    }
}
