package com.imaginamos.farmatodo.networking.models.addresses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddressPredictionReq {

    private String city;
    private String address;
    private String country;
    private String deliveryType;

    public AddressPredictionReq(String city, String address) {
        this.city = city;
        this.address = address;
    }

    public AddressPredictionReq(String city, String address, String deliveryType, String country) {
        this.city = city;
        this.address = address;
        this.deliveryType = deliveryType;
        this.country = country;
    }

    public AddressPredictionReq() {
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    @Override
    public String toString() {
        return "AddressPredictionReq{" +
                "city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", country='" + country + '\'' +
                ", deliveryType='" + deliveryType + '\'' +
                '}';
    }
    public String toStringJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
