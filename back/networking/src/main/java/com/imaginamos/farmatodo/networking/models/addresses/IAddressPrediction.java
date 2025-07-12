package com.imaginamos.farmatodo.networking.models.addresses;

/**
 * Created by SergioRojas on 6/04/18.
 */

public interface IAddressPrediction {

    boolean isSuccessful();

    void setStandardCity(String city);

    String getStandardAddress();

    String getStandardCity();

    String getStandardZone();

    String getStandardLat();

    String getStandardLng();

}
