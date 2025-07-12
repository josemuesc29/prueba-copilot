package com.imaginamos.farmatodo.model.payment;

import com.imaginamos.farmatodo.model.customer.CreditCard;

public class PaymentMethodsV2Data {
    private String id;
    private String paymentMethod;
    private String description;
    private String mediaDescription;
    private String infoPaymentMethod;
    private int positionIndex;
    private Boolean status;
    private Boolean defaultPaymentMethod;
    private PSEResponse dataForPSE;
    private CreditCard creditCard;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfoPaymentMethod() {
        return infoPaymentMethod;
    }

    public void setInfoPaymentMethod(String infoPaymentMethod) {
        this.infoPaymentMethod = infoPaymentMethod;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getDefaultPaymentMethod() {
        return defaultPaymentMethod;
    }

    public void setDefaultPaymentMethod(Boolean defaultPaymentMethod) {
        this.defaultPaymentMethod = defaultPaymentMethod;
    }

    public PSEResponse getDataForPSE() {
        return dataForPSE;
    }

    public void setDataForPSE(PSEResponse dataForPSE) {
        this.dataForPSE = dataForPSE;
    }

    public String getMediaDescription() {
        return mediaDescription;
    }

    public void setMediaDescription(String mediaDescription) {
        this.mediaDescription = mediaDescription;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }
}
