package com.imaginamos.farmatodo.model.payment;


import java.util.List;

public class PaymentMethodV2FTDResponse {
    private String code;
    private String message;
    private PaymentMethodsV2Data defaultPaymentMethod;
    private List<PaymentMethodsV2Data> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PaymentMethodsV2Data> getData() {
        return data;
    }

    public void setData(List<PaymentMethodsV2Data> data) {
        this.data = data;
    }

    public PaymentMethodsV2Data getDefaultPaymentMethod() {
        return defaultPaymentMethod;
    }

    public void setDefaultPaymentMethod(PaymentMethodsV2Data defaultPaymentMethod) {
        this.defaultPaymentMethod = defaultPaymentMethod;
    }
}
