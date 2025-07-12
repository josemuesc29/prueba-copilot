package com.imaginamos.farmatodo.model.algolia;

public class DeliveryTimeLabelMobile {

    private String cartLabel;
    private String cartLabelTime;
    private String cartProvider;
    private String summaryLabel;
    private String summaryProvider;

    public String getCartLabel() {
        return cartLabel;
    }

    public void setCartLabel(String cartLabel) {
        this.cartLabel = cartLabel;
    }

    public String getCartLabelTime() {
        return cartLabelTime;
    }

    public void setCartLabelTime(String cartLabelTime) {
        this.cartLabelTime = cartLabelTime;
    }

    public String getCartProvider() {
        return cartProvider;
    }

    public void setCartProvider(String cartProvider) {
        this.cartProvider = cartProvider;
    }

    public String getSummaryLabel() {
        return summaryLabel;
    }

    public void setSummaryLabel(String summaryLabel) {
        this.summaryLabel = summaryLabel;
    }

    public String getSummaryProvider() {
        return summaryProvider;
    }

    public void setSummaryProvider(String summaryProvider) {
        this.summaryProvider = summaryProvider;
    }
}
