package com.imaginamos.farmatodo.model.talonone;

public enum BestOfferEnum {
    PRIME("PRIME"),
    TALON_ONE ("TALON_ONE"),
    RPM("RPM");

    private final String offer;

    BestOfferEnum(String offer) {
        this.offer = offer;
    }

    public String getOffer() {
        return offer;
    }
}
