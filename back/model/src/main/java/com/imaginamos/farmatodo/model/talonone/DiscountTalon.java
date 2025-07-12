package com.imaginamos.farmatodo.model.talonone;

public class DiscountTalon {
    private String sku;
    private Double offerPrice;
    private String offerText;
    private String offerDescription;
    private int position;

    public DiscountTalon(String sku, Double offerPrice, String offerText, String offerDescription, int position) {
        this.sku = sku;
        this.offerPrice = offerPrice;
        this.offerText = offerText;
        this.offerDescription = offerDescription;
        this.position = position;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public String getOfferDescription() {
        return offerDescription;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "DiscountTalon{" +
                "sku='" + sku + '\'' +
                ", offerPrice=" + offerPrice +
                ", offerText='" + offerText + '\'' +
                ", offerDescription='" + offerDescription + '\'' +
                ", position=" + position +
                '}';
    }
}
