package com.imaginamos.farmatodo.model.talonone;


public class PetalComplexOffer {
    boolean isComplexOffer;
    String offerText;
    String offerDescription;

    public PetalComplexOffer() {
    }

    public boolean isComplexOffer() {
        return isComplexOffer;
    }

    public void setComplexOffer(boolean complexOffer) {
        isComplexOffer = complexOffer;
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
}
