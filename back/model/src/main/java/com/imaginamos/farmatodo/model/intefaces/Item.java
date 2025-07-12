package com.imaginamos.farmatodo.model.intefaces;

public interface Item {
    String getMediaDescription();
    long getId();
    int getQuantitySold();
    Double getFullPrice();
    String getCategorie();
    String getMarca();
    String getSubCategory();
    void setOfferPrice(Double offerPrice);
    void setOfferText(String offerText);
    void setOfferDescription(String offerDescription);
    String getBrand();

}
